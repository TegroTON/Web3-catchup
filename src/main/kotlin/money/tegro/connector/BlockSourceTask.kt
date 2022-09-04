package money.tegro.connector

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.pub.PublicKeyEd25519
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.Block
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.crypto.base64
import org.ton.lite.client.LiteClient
import org.ton.tlb.storeTlb
import kotlin.coroutines.CoroutineContext

abstract class BlockSourceTask : SourceTask(), CoroutineScope {
    lateinit var liteClient: LiteClient

    private lateinit var configuration: BlockSourceTaskConfig
    private var queue = ArrayDeque<SourceRecord>(1024)
    private val job = launch {
        // Wait for initialization
        while (!this@BlockSourceTask::liteClient.isInitialized) {
            delay(1000)
        }

        processBlock(masterchainBlocks())
            .collect {
                queue.addLast(
                    SourceRecord(
                        mapOf(
                            "workchain" to it.info.shard.workchain_id,
                        ),
                        mapOf(
                            "seqno" to it.info.seq_no,
                        ),
                        configuration.topic,
                        Schema.BYTES_SCHEMA,
                        BagOfCells(CellBuilder.createCell { storeTlb(Block, it) }).toByteArray(),
                    )
                )
            }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting block source connector task")
        liteClient = LiteClient(
            LiteServerDesc(
                PublicKeyEd25519.of(base64(configuration.publicKey)),
                configuration.ipv4,
                configuration.port
            )
        )
        job.start()
    }

    override fun stop() {
        logger.info("stopping block source connector task")
        job.cancel()
    }

    override fun poll(): MutableList<SourceRecord> =
        sequence {
            while (queue.isNotEmpty())
                yield(queue.removeFirst())
        }
            .toMutableList()

    override fun version(): String = version

    abstract fun masterchainBlocks(): Flow<TonNodeBlockIdExt>

    @OptIn(FlowPreview::class)
    private fun processBlock(input: Flow<TonNodeBlockIdExt>) =
        input
            .mapNotNull {
                try {
                    liteClient.getBlock(it)?.let(::listOf)
                } catch (e: Exception) {
                    logger.warn("couldn't get masterchain block seqno={}", it, e)
                    null
                }
            }
            .runningReduce { accumulator, value ->
                val lastMcShards = accumulator.last().extra.custom.value?.shard_hashes
                    ?.nodes()
                    .orEmpty()
                    .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }

                value.last().extra.custom.value?.shard_hashes
                    ?.nodes()
                    .orEmpty()
                    .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }
                    .flatMap { curr ->
                        (lastMcShards.getOrDefault(curr.key, curr.value).seq_no + 1u..curr.value.seq_no)
                            .map {
                                TonNodeBlockId(
                                    curr.key,
                                    curr.value.next_validator_shard.toLong() /* Shard.ID_ALL */,
                                    it.toInt()
                                )
                            } // TODO
                    }
                    .mapNotNull {
                        try {
                            liteClient.lookupBlock(it)?.let { liteClient.getBlock(it) }
                        } catch (e: Exception) {
                            logger.warn("couldn't get block workchain={} seqno={}", it.workchain, it.seqno, e)
                            null
                        }
                    }
                    .plus(value)
            }
            .flatMapConcat { it.asFlow() }
            .onEach {
                logger.debug("block workchain={} seqno={}", it.info.shard.workchain_id, it.info.seq_no)
            }

    companion object : KLogging()
}

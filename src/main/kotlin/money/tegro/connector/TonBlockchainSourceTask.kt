package money.tegro.connector

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.pub.PublicKeyEd25519
import org.ton.api.tonnode.Shard
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

class TonBlockchainSourceTask : SourceTask(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private lateinit var configuration: TonBlockchainSourceTaskConfig
    private lateinit var liteClient: LiteClient
    private var queue = ArrayDeque<SourceRecord>(1024)

    private val job = launch {
        // Wait for initialization
        while (!this@TonBlockchainSourceTask::liteClient.isInitialized) {
            delay(1000)
        }

        processBlock(liveMasterchainBlocks())
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

    override fun version(): String = version

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting ton blockchain connector task")
        try {
            configuration = TonBlockchainSourceTaskConfig(props ?: mapOf())
            liteClient = LiteClient(
                LiteServerDesc(
                    PublicKeyEd25519.of(base64(configuration.publicKey)),
                    configuration.ipv4,
                    configuration.port
                )
            )
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start ton blockchain connector task due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting ton blockchain connector task", ce)
        }
        job.start()
    }

    override fun stop() {
        logger.info("stopping ton blockchain connector task")
        job.cancel()
    }

    override fun poll(): MutableList<SourceRecord> =
        sequence {
            while (queue.isNotEmpty())
                yield(queue.removeFirst())
        }
            .toMutableList()

    private fun liveMasterchainBlocks() = flow {
        while (currentCoroutineContext().isActive) {
            emit(liteClient.getLastBlockId()) // Masterchain blocks
        }
    }
        .distinctUntilChanged()

    private fun historicalMasterchainBlocks(start: Int = 0, end: Int? = null) =
        flow {
            (start until (end ?: liteClient.getLastBlockId().seqno))
                .forEach {
                    liteClient.lookupBlock(TonNodeBlockId.of(-1, Shard.ID_ALL, it))?.let { emit(it) }
                }
        }
            .distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private suspend fun processBlock(input: Flow<TonNodeBlockIdExt>) =
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

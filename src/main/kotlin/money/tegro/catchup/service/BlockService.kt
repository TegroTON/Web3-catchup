package money.tegro.catchup.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import money.tegro.catchup.properties.BlockServiceProperties
import mu.KLogging
import org.springframework.beans.factory.DisposableBean
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.Block
import org.ton.block.ShardDescr
import org.ton.lite.client.LiteClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

abstract class BlockService(
    val liteClient: LiteClient,
    open val properties: BlockServiceProperties
) : CoroutineScope, DisposableBean {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    protected val blockQueue = LinkedBlockingQueue<Block>()
    fun pollBlocks(): List<Block> = mutableListOf<Block>().apply { blockQueue.drainTo(this) }

    abstract fun masterchainBlocks(): Flow<TonNodeBlockIdExt>

    override fun destroy() {
        backgroundJob.cancel()
    }

    private val backgroundJob = launch {
        masterchainBlocks()
            .processMasterchainBlocks()
    }

    private suspend fun Flow<TonNodeBlockIdExt>.processMasterchainBlocks() =
        this.mapNotNull {
            try {
                liteClient.getBlock(it)
            } catch (e: Exception) {
                logger.warn("couldn't get masterchain block seqno={}", it, e)
                null
            }
        }
            .map(::getShardchainBlocks)
            .collect { blocks ->
                blocks.forEach {
                    logger.info("block workchain={} seqno={}", it.info.shard.workchain_id, it.info.seq_no)
                }

                blockQueue.addAll(blocks)
            }

    private val lastMasterchainShards = ConcurrentHashMap<Int, ShardDescr>()

    private suspend fun getShardchainBlocks(masterchainBlock: Block): List<Block> {
        val masterchainShards = masterchainBlock.extra.custom.value?.shard_hashes
            ?.nodes()
            .orEmpty()
            .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }

        val shardchainBlocks = masterchainShards
            .flatMap { (workchain, shard) ->
                (lastMasterchainShards.getOrDefault(workchain, shard).seq_no + 1u..shard.seq_no)
                    .map { seqno ->
                        TonNodeBlockId(
                            workchain,
                            shard.next_validator_shard.toLong(), /* Shard.ID_ALL */ // TODO
                            seqno.toInt(),
                        )
                    }
            }
            .asFlow()
            .mapNotNull {
                try {
                    liteClient.lookupBlock(it)?.let { liteClient.getBlock(it) }
                } catch (e: Exception) {
                    logger.warn("couldn't get block workchain={} seqno={}", it.workchain, it.seqno, e)
                    null
                }
            }
            .toList()

        lastMasterchainShards.clear()
        lastMasterchainShards.putAll(masterchainShards)

        return listOf(masterchainBlock) + shardchainBlocks
    }

    companion object : KLogging()
}

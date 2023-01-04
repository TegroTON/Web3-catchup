package money.tegro.catchup.service

import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import money.tegro.catchup.properties.BlockServiceProperties
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.lite.api.liteserver.LiteServerTransactionInfo
import org.ton.lite.client.LiteClient
import org.ton.tlb.storeTlb
import java.util.concurrent.ConcurrentHashMap

@Service
class BlockService(
    private val liteClient: LiteClient,
    private val queueMessagingTemplate: QueueMessagingTemplate,
    private val blockServiceProperties: BlockServiceProperties,
) {
    final val latestBlockIds = flow {
        while (currentCoroutineContext().isActive) {
            emit(liteClient.getLastBlockId())
            kotlinx.coroutines.time.delay(blockServiceProperties.pollRate)
        }
    }
        .distinctUntilChanged()
        .onEach { logger.debug { "latest masterchain block seqno=${it.seqno}" } }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("latestBlockIds")), SharingStarted.Eagerly)

    @OptIn(FlowPreview::class)
    final val liveBlocks = latestBlockIds
        .mapNotNull { id ->
            try {
                liteClient.getBlock(id)?.let { id to it } // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get masterchain block seqno=${id.seqno}" }
                null
            }
        }
        .flatMapConcat { (id, block) -> getShardchainBlocks(id, block) }
        .onEach { (id, _) ->
            logger.info { "block workchain=${id.workchain} seqno=${id.seqno}" }
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("liveBlocks")), SharingStarted.Eagerly, 64)

    private val lastMasterchainShards = ConcurrentHashMap<Int, ShardDescr>()

    @OptIn(FlowPreview::class)
    private suspend fun getShardchainBlocks(
        masterchainBlockId: TonNodeBlockIdExt,
        masterchainBlock: Block
    ): Flow<Pair<TonNodeBlockIdExt, Block>> {
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
                            Shard.ID_ALL, // shard.next_validator_shard.toLong(),
                            seqno.toInt(),
                        )
                    }
            }
            .asFlow()
            .mapNotNull { id ->
                try {
                    liteClient.lookupBlock(id) // TODO: Retries
                } catch (e: Exception) {
                    logger.warn(e) { "couldn't get shardchain block id workchain=${id.workchain} seqno=${id.seqno}" }
                    null
                }
            }
            .mapNotNull { id ->
                try {
                    liteClient.getBlock(id)?.let { id to it } // TODO: Retries
                } catch (e: Exception) {
                    logger.warn(e) { "couldn't get shardchain block workchain=${id.workchain} seqno=${id.seqno}" }
                    null
                }
            }

        lastMasterchainShards.clear()
        lastMasterchainShards.putAll(masterchainShards)

        return flowOf(masterchainBlockId to masterchainBlock).onCompletion() { emitAll(shardchainBlocks) }
    }

    @OptIn(FlowPreview::class)
    private final val liveTransactions = liveBlocks
        .flatMapConcat { (id, block) ->
            block.extra.account_blocks.nodes()
                .flatMap { (account, _) ->
                    account.transactions.nodes().map { (transaction, _) -> id to transaction }
                }
                .asFlow()
        }
        .onEach { (_, transaction) ->
            when (val info = transaction.in_msg.value?.info) {
                is ExtInMsgInfo -> {
                    logger.debug { "${info.src} -> (ext in) -> ${info.dest}" }
                }

                is ExtOutMsgInfo -> {
                    logger.debug { "${info.src} -> (ext out) -> ${info.dest}" }
                }

                is IntMsgInfo -> {
                    logger.debug { "${info.src} -> (in) -> ${info.dest}" }
                }

                null -> {}
            }
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("liveTransactions")), SharingStarted.Eagerly, 64)


    private val backgroundJob = CoroutineScope(Dispatchers.Default + CoroutineName("backgroundJob")).launch {
        liveTransactions.collect { (id, transaction) ->
            val transactionCell = CellBuilder.createCell { storeTlb(Transaction, transaction) }
            queueMessagingTemplate.convertAndSend(
                "transactions",
                LiteServerTransactionInfo(
                    id,
                    BagOfCells(CellBuilder.createMerkleProof(transactionCell)).toByteArray(),
                    BagOfCells(transactionCell).toByteArray(),
                )
            )
        }
    }

    companion object : KLogging()
}

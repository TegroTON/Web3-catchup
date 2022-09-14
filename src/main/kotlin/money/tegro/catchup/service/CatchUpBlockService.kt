package money.tegro.catchup.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import money.tegro.catchup.properties.CatchUpBlockServiceProperties
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Service
@Scope("prototype")
@ConditionalOnProperty("catchup.blocks.catch-up.enabled", havingValue = "true")
class CatchUpBlockService(
    liteClient: LiteClient,
    override val properties: CatchUpBlockServiceProperties,
) : BlockService("catch-up", liteClient, properties) {
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            for (seqno in properties.startSeqno until (properties.endSeqno ?: liteClient.getLastBlockId().seqno)) {
                liteClient.lookupBlock(TonNodeBlockId(workchain = -1, shard = Shard.ID_ALL, seqno = seqno))
                    ?.let { emit(it) }
                    ?: logger.warn { "failed to lookup masterchain block seqno=$seqno" }

                do {
                    yield()
                } while (blockQueue.size >= properties.maxQueue)
            }
        }

    companion object : KLogging()
}

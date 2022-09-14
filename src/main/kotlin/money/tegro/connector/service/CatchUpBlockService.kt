package money.tegro.connector.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import money.tegro.connector.properties.CatchUpBlockServiceProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Service
@Scope("prototype")
@ConditionalOnProperty("service.blocks.catch-up.enabled", havingValue = "true")
class CatchUpBlockService(
    liteClient: LiteClient,
    override val properties: CatchUpBlockServiceProperties,
) : BlockService(liteClient, properties) {
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            for (seqno in 0 until liteClient.getLastBlockId().seqno) {
                liteClient.lookupBlock(
                    TonNodeBlockId(
                        workchain = -1,
                        shard = Shard.ID_ALL,
                        seqno = seqno,
                    )
                )?.let { emit(it) }
                do {
                    yield()
                } while (blockQueue.size >= properties.maxQueue)
            }
        }
}

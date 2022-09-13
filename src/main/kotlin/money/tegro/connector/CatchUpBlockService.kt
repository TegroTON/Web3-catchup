package money.tegro.connector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Service
@Scope("prototype")
class CatchUpBlockService(liteClient: LiteClient) : BlockService(liteClient) {
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            (0 until liteClient.getLastBlockId().seqno)
                .forEach { seqno ->
                    liteClient.lookupBlock(
                        TonNodeBlockId(
                            workchain = -1,
                            shard = Shard.ID_ALL,
                            seqno = seqno,
                        )
                    )?.let { emit(it) }
                    yield()
                }
        }
}

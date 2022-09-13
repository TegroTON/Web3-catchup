package money.tegro.connector

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Service
@Scope("prototype")
class LiveBlockService(liteClient: LiteClient) : BlockService(liteClient) {
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(liteClient.getLastBlockId())
                delay(1_000)
            }
        }
            .distinctUntilChanged()
}

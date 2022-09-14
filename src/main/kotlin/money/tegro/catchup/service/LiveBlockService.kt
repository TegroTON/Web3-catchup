package money.tegro.catchup.service

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import money.tegro.catchup.properties.LiveBlockServiceProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Service
@Scope("prototype")
@ConditionalOnProperty("catchup.blocks.live.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(CatchUpBlockService::class)
class LiveBlockService(
    liteClient: LiteClient,
    override val properties: LiveBlockServiceProperties,
) : BlockService("live", liteClient, properties) {
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(liteClient.getLastBlockId())
                delay(properties.pollRate)
            }
        }
            .distinctUntilChanged()
}

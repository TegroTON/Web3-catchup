package money.tegro.connector

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException
import org.ton.api.tonnode.TonNodeBlockIdExt

class LiveBlockSourceTask : BlockSourceTask() {
    lateinit var configuration: LiveBlockSourceTaskConfig

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting live block source connector task")
        try {
            configuration = LiveBlockSourceTaskConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start live block source connector task due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting live block source connector task", ce)
        }
        super.start(props)
    }
    
    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(liteClient.getLastBlockId())
            }
        }
            .distinctUntilChanged()
}

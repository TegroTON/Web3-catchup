package money.tegro.connector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockIdExt

class ArchiveBlockSourceTask : BlockSourceTask() {
    lateinit var configuration: ArchiveBlockSourceTaskConfig

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting archive block source connector task")
        try {
            configuration = ArchiveBlockSourceTaskConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start archive block source connector task due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting archive block source connector task", ce)
        }
        super.start(props)
    }

    override fun masterchainBlocks(): Flow<TonNodeBlockIdExt> =
        flow {
            (configuration.startBlock until (configuration.endBlock ?: liteClient.getLastBlockId().seqno))
                .forEach { seqno ->
                    liteClient.lookupBlock(TonNodeBlockIdExt(workchain = -1, shard = Shard.ID_ALL, seqno = seqno))
                        ?.let { emit(it) }
                }
        }
}

package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException

class ArchiveBlockSourceConnector : BlockSourceConnector() {
    lateinit var configuration: ArchiveBlockSourceConnectorConfig

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting archive block source connector")
        try {
            configuration = ArchiveBlockSourceConnectorConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start archive block source connector due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting archive block source connector", ce)
        }
        super.start(props)
    }

    override fun taskClass(): Class<out Task> = ArchiveBlockSourceTask::class.java

    override fun config(): ConfigDef = ArchiveBlockSourceConnectorConfig.definition()

    companion object : KLogging()
}

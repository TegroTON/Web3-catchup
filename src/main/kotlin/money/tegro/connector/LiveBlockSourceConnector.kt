package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException

class LiveBlockSourceConnector : BlockSourceConnector() {
    lateinit var configuration: LiveBlockSourceConnectorConfig

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting live block source connector")
        try {
            configuration = LiveBlockSourceConnectorConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start live block source connector due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting live block source connector", ce)
        }
        super.start(props)
    }

    override fun taskClass(): Class<out Task> = LiveBlockSourceTask::class.java

    override fun config(): ConfigDef = LiveBlockSourceConnectorConfig.definition()

    companion object : KLogging()
}

package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceConnector

abstract class BlockSourceConnector() : SourceConnector() {
    private lateinit var configuration: BlockSourceConnectorConfig

    override fun config(): ConfigDef = BlockSourceConnectorConfig.definition()

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting block source connector")
        try {
            configuration = BlockSourceConnectorConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start block source connector due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting block source connector", ce)
        }
    }

    override fun stop() {
        logger.info("stopping block source connector")
    }

    override fun taskConfigs(maxTasks: Int): MutableList<MutableMap<String, String>> {
        if (!this::configuration.isInitialized) {
            throw ConnectException("Connector configuration has not been initialized.")
        }

        return mutableListOf(configuration.originalsStrings())
    }

    override fun version(): String = version

    companion object : KLogging()
}

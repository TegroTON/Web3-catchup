package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceConnector

class TonBlockchainSourceConnector : SourceConnector() {
    private lateinit var configuration: TonBlockchainSourceConnectorConfig

    override fun version(): String = version

    override fun start(props: MutableMap<String, String>?) {
        logger.info("starting ton blockchain connector")
        try {
            configuration = TonBlockchainSourceConnectorConfig(props ?: mapOf())
        } catch (ce: ConfigException) {
            throw ConnectException("Couldn't start ton blockchain connector due to configuration error", ce)
        } catch (ce: Exception) {
            throw ConnectException("An error occurred when starting ton blockchain connector", ce)
        }
    }

    override fun taskClass(): Class<out Task> = TonBlockchainSourceTask::class.java

    override fun taskConfigs(maxTasks: Int): MutableList<MutableMap<String, String>> {
        if (!this::configuration.isInitialized) {
            throw ConnectException("Connector configuration has not been initialized.")
        }

        return mutableListOf(configuration.originalsStrings())
    }

    override fun stop() {
        logger.info("stopping ton blockchain connector")
    }

    override fun config(): ConfigDef = TonBlockchainSourceConnectorConfig.definition()

    companion object : KLogging()
}

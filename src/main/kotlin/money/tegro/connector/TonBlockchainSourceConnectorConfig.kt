package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef

open class TonBlockchainSourceConnectorConfig(config: ConfigDef, parsedConfig: Map<String, String>) :
    AbstractConfig(config, parsedConfig) {
    constructor(parsedConfig: Map<String, String>) : this(definition(), parsedConfig)

    val topic by lazy { getString(TOPIC) }

    companion object : KLogging() {
        const val PREFIX = "ton"

        const val TOPIC = PREFIX + ".topic"
        const val TOPIC_DOC = "Topic to post blocks to"
        const val TOPIC_DISPLAY = "Topic"

        const val GROUP = "Connector"

        @JvmStatic
        fun definition(): ConfigDef {
            var order = 0
            return ConfigDef()
                .define(
                    TOPIC,
                    ConfigDef.Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    TOPIC_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    TOPIC_DISPLAY,
                )
        }
    }
}

package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef

class LiveBlockSourceTaskConfig(config: ConfigDef, parsedConfig: Map<String, String>) :
    BlockSourceTaskConfig(config, parsedConfig) {
    constructor(parsedConfig: Map<String, String>) : this(definition(), parsedConfig)

    companion object : KLogging() {
        @JvmStatic
        fun definition() = BlockSourceTaskConfig.definition()
    }
}

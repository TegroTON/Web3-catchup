package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef

class ArchiveBlockSourceTaskConfig(config: ConfigDef, parsedConfig: Map<String, String>) :
    BlockSourceTaskConfig(config, parsedConfig) {
    constructor(parsedConfig: Map<String, String>) : this(definition(), parsedConfig)

    val startBlock by lazy { getInt(BLOCK_START) }
    val endBlock by lazy { values().get(BLOCK_END) as? Int }

    companion object : KLogging() {
        const val BLOCK_START = "block.start"
        const val BLOCK_START_DOC = "First block to get from the archival node"
        const val BLOCK_START_DISPLAY = "Start block"

        const val BLOCK_END = "block.end"
        const val BLOCK_END_DOC = "Last block to get from the archival node"
        const val BLOCK_END_DISPLAY = "End block"

        const val GROUP = "Connector Task Archival"

        @JvmStatic
        fun definition(): ConfigDef {
            var order = 0
            return BlockSourceTaskConfig.definition()
                .define(
                    BLOCK_START,
                    ConfigDef.Type.INT,
                    0,
                    ConfigDef.Importance.HIGH,
                    BLOCK_START_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    BLOCK_START_DISPLAY,
                )
                .define(
                    BLOCK_END,
                    ConfigDef.Type.INT,
                    0,
                    ConfigDef.Importance.HIGH,
                    BLOCK_END_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    BLOCK_END_DISPLAY,
                )
        }
    }
}

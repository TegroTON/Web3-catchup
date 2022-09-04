package money.tegro.connector

import mu.KLogging
import org.apache.kafka.common.config.ConfigDef

open class BlockSourceTaskConfig(config: ConfigDef, parsedConfig: Map<String, String>) :
    BlockSourceConnectorConfig(config, parsedConfig) {
    constructor(parsedConfig: Map<String, String>) : this(definition(), parsedConfig)

    val ipv4 by lazy { getInt(LITE_SERVER_IPv4) }
    val port by lazy { getInt(LITE_SERVER_PORT) }
    val publicKey by lazy { getString(LITE_SERVER_PKEY) }

    companion object : KLogging() {
        const val LITE_SERVER_IPv4 = "lite.server.ipv4"
        const val LITE_SERVER_IPv4_DOC = "IPv4 address of the lite-server to connect to"
        const val LITE_SERVER_IPv4_DISPLAY = "Lite-server IPv4 address"

        const val LITE_SERVER_PORT = "lite.server.port"
        const val LITE_SERVER_PORT_DOC = "Port of the lite-server to connect to"
        const val LITE_SERVER_PORT_DISPLAY = "Lite-server port"

        const val LITE_SERVER_PKEY = "lite.server.pkey"
        const val LITE_SERVER_PKEY_DOC = "Public key of the lite-server to connect to"
        const val LITE_SERVER_PKEY_DISPLAY = "Lite-server public key"

        const val GROUP = "Connector Task"

        @JvmStatic
        fun definition(): ConfigDef {
            var order = 0
            return BlockSourceConnectorConfig.definition()
                .define(
                    LITE_SERVER_IPv4,
                    ConfigDef.Type.INT,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    LITE_SERVER_IPv4_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    LITE_SERVER_IPv4_DISPLAY,
                )
                .define(
                    LITE_SERVER_PORT,
                    ConfigDef.Type.INT,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    LITE_SERVER_PORT_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    LITE_SERVER_PORT_DISPLAY,
                )
                .define(
                    LITE_SERVER_PKEY,
                    ConfigDef.Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    LITE_SERVER_PKEY_DOC,
                    GROUP,
                    ++order,
                    ConfigDef.Width.LONG,
                    LITE_SERVER_PKEY_DISPLAY,
                )
        }
    }
}

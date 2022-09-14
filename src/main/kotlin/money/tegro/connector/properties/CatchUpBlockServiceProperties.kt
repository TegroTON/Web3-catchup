package money.tegro.connector.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConstructorBinding
@ConfigurationProperties(prefix = "service.blocks.catch-up")
class CatchUpBlockServiceProperties(
    @DefaultValue("false")
    val enabled: Boolean,

    @DefaultValue("16")
    val maxQueue: Long,
) : BlockServiceProperties

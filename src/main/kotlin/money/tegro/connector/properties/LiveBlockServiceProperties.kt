package money.tegro.connector.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConstructorBinding
@ConfigurationProperties(prefix = "service.blocks.live")
class LiveBlockServiceProperties(
    @DefaultValue("true")
    val enabled: Boolean,
) : BlockServiceProperties

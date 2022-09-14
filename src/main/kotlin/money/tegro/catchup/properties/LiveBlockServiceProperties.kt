package money.tegro.catchup.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConstructorBinding
@ConfigurationProperties(prefix = "catchup.blocks.live")
class LiveBlockServiceProperties(
    @DefaultValue("true")
    val enabled: Boolean,
) : BlockServiceProperties

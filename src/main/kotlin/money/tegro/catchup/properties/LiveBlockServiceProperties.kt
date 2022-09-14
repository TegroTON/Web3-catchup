package money.tegro.catchup.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "catchup.blocks.live")
class LiveBlockServiceProperties(
    @DefaultValue("true")
    val enabled: Boolean,

    @DefaultValue("PT2S")
    val pollRate: Duration,
) : BlockServiceProperties

package money.tegro.catchup.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "catchup.blocks")
@ConstructorBinding
data class BlockServiceProperties(
    @DefaultValue("PT2S")
    val pollRate: Duration,

    @DefaultValue("transactions")
    val queueName: String,
)

package money.tegro.catchup.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConstructorBinding
@ConfigurationProperties(prefix = "catchup.blocks.catch-up")
class CatchUpBlockServiceProperties(
    @DefaultValue("false")
    val enabled: Boolean,

    @DefaultValue("16")
    val maxQueue: Long,

    @DefaultValue("0")
    val startSeqno: Int,

    val endSeqno: Int?,
) : BlockServiceProperties

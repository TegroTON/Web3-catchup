package money.tegro.catchup.configuration

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import money.tegro.catchup.properties.LiteClientProperties
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.ton.lite.client.LiteClient


@Configuration
class LiteClientConfiguration(
    private val liteClientProperties: LiteClientProperties,
) {
    @Bean
    @Scope("prototype")
    fun liteClient() = LiteClient(
        Dispatchers.IO + CoroutineName("liteClient"),
        liteClientProperties.toLiteServerDesc(),
    )

    companion object : KLogging()
}

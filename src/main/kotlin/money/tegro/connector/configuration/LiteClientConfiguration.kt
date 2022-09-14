package money.tegro.connector.configuration

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.lite.client.LiteClient

@Configuration
class LiteClientConfiguration {
    @Bean
    fun liteClient() = LiteClient(Json {
        ignoreUnknownKeys = true
    }.decodeFromStream<LiteClientConfigGlobal>(ClassPathResource("config-sandbox.json").inputStream))

    companion object : KLogging()
}

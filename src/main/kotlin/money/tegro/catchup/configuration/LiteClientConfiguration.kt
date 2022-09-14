package money.tegro.catchup.configuration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.lite.client.LiteClient


@Configuration
class LiteClientConfiguration(
    @Value("\${LITE_CLIENT_CONFIG:classpath:config-sandbox.json}")
    private val jsonConfig: Resource
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun liteClient() = LiteClient(
        Json {
            ignoreUnknownKeys = true
        }
            .decodeFromStream<LiteClientConfigGlobal>(jsonConfig.inputStream))

    companion object : KLogging()
}

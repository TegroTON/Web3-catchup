package money.tegro.catchup.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.KotlinSerializationJsonMessageConverter

@Configuration
class MessageConverterConfiguration {
    @Bean
    fun kotlinSerializationMessageConverter() = KotlinSerializationJsonMessageConverter()
}

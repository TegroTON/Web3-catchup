package money.tegro.catchup.configuration

import com.amazonaws.regions.AwsRegionProviderChain
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfiguration {
    @Bean
    fun regionProvider() = object : AwsRegionProviderChain() {
        override fun getRegion() = "ru-central1"
    }
}

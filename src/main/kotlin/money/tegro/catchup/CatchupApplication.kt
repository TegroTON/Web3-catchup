package money.tegro.catchup

import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("money.tegro.catchup.properties")
@SpringBootApplication
class CatchupApplication {
    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<CatchupApplication>(*args)
        }
    }
}


package money.tegro.catchup

import mu.KLogging
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableRabbit
@EnableScheduling
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


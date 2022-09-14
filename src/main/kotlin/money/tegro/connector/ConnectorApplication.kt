package money.tegro.connector

import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("money.tegro.connector.properties")
@SpringBootApplication
class ConnectorApplication {
    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ConnectorApplication>(*args)
        }
    }
}


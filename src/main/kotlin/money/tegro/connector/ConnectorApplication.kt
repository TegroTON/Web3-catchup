package money.tegro.connector

import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.function.Supplier

@SpringBootApplication
class ConnectorApplication {
    @Bean
    fun blocks(liveBlockService: LiveBlockService, catchUpBlockService: CatchUpBlockService) =
        Supplier { liveBlockService.pollBlocks() + catchUpBlockService.pollBlocks() }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ConnectorApplication>(*args)
        }
    }
}


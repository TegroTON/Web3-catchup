package money.tegro.connector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.asFlux
import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.ton.block.Block
import reactor.core.publisher.Flux
import java.util.function.Supplier

@SpringBootApplication
class ConnectorApplication {
    @Bean
    fun blocks(liveBlocks: Flow<Block>) =
        Supplier<Flux<Block>> { liveBlocks.asFlux() }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ConnectorApplication>(*args)
        }
    }
}


package money.tegro.connector.configuration

import money.tegro.connector.service.BlockService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@Configuration
class SupplierConfiguration {
    @Bean
    fun blocks(blockService: BlockService) = Supplier { blockService.pollBlocks() }
}

package money.tegro.catchup.configuration

import money.tegro.catchup.service.BlockService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@Configuration
class SupplierConfiguration {
    @Bean
    fun blocks(blockService: BlockService) = Supplier { blockService.pollBlocks() }
}

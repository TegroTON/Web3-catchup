package money.tegro.catchup.configuration

import money.tegro.catchup.service.BlockService
import money.tegro.catchup.service.CatchUpBlockService
import mu.KLogging
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.ton.block.Block
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.tlb.storeTlb

@Configuration
class SupplierConfiguration(
    private val rabbitTemplate: RabbitTemplate,
    private val blockService: BlockService
) {
    @Async
    @Scheduled(initialDelay = 2_000, fixedRate = 1_000)
    fun blocks() {
        blockService.pollBlocks()
            .forEach {
                val body = BagOfCells(CellBuilder.createCell { storeTlb(Block, it) }).toByteArray()
                rabbitTemplate.send(
                    "blocks",
                    if (blockService is CatchUpBlockService) "catch-up" else "live",
                    MessageBuilder
                        .withBody(body)
                        .setContentTypeIfAbsentOrDefault("application/octet-stream")
                        .setContentLengthIfAbsent(body.size.toLong())
                        .build()
                )
            }
    }

    companion object : KLogging()
}

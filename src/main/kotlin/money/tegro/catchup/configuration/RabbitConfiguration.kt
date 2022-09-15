package money.tegro.catchup.configuration

import money.tegro.catchup.service.BlockService
import mu.KLogging
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.AbstractMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.ton.block.Block
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

@Configuration
class RabbitConfiguration {
    @Bean
    fun exchange(): Exchange =
        ExchangeBuilder.topicExchange("blocks")
            .durable(true)
            .build<DirectExchange>()

    @Bean
    fun messageConverter() = object : AbstractMessageConverter() {
        override fun createMessage(`object`: Any, messageProperties: MessageProperties): Message {
            val body = BagOfCells(CellBuilder.createCell { storeTlb(Block, `object` as Block) }).toByteArray()
            return MessageBuilder
                .withBody(body)
                .setContentTypeIfAbsentOrDefault(MessageProperties.CONTENT_TYPE_BYTES)
                .setContentLengthIfAbsent(body.size.toLong())
                .build()
        }

        override fun fromMessage(message: Message): Any =
            BagOfCells(message.body).roots.first().parse { loadTlb(Block) }
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter, exchange: Exchange) =
        RabbitTemplate(connectionFactory).apply {
            setMessageConverter(messageConverter)
            setExchange(exchange.name)
        }

    @Bean
    fun blockProducer(rabbitTemplate: RabbitTemplate, blockService: BlockService): Runnable =
        object : InitializingBean, Runnable {
            override fun afterPropertiesSet() {
                rabbitTemplate.routingKey = blockService.routingKey
            }

            @Async
            @Scheduled(fixedRateString = "\${catchup.blocks.rate:1000}")
            override fun run() {
                blockService.pollBlocks().forEach(rabbitTemplate::convertAndSend)
            }
        }

    companion object : KLogging()
}

package money.tegro.connector

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.bigint.BigInt
import org.ton.lite.client.LiteClient

@Configuration
class BlockchainConfiguration {
    @Bean
    fun liteClient() = LiteClient(Json {
        ignoreUnknownKeys = true
    }.decodeFromStream<LiteClientConfigGlobal>(ClassPathResource("config-sandbox.json").inputStream))

    @OptIn(FlowPreview::class)
    @Bean
    fun liveBlocks(liteClient: LiteClient) =
        merge(
            flow { // Old masterchain blocks
                (0 until liteClient.getLastBlockId().seqno)
                    .forEach { seqno ->
                        liteClient.lookupBlock(
                            TonNodeBlockId(
                                workchain = -1,
                                shard = Shard.ID_ALL,
                                seqno = seqno,
                            )
                        )?.let { emit(it) }
                    }
            },
            flow { // Live blocks
                while (currentCoroutineContext().isActive) {
                    emit(liteClient.getLastBlockId())
                    delay(1_000)
                }
            }
                .distinctUntilChanged()
        )
            .mapNotNull {
                try {
                    liteClient.getBlock(it)?.let(::listOf)
                } catch (e: Exception) {
                    logger.warn("couldn't get masterchain block seqno={}", it, e)
                    null
                }
            }
            .runningReduce { accumulator, value ->
                val lastMcShards = accumulator.last().extra.custom.value?.shard_hashes
                    ?.nodes()
                    .orEmpty()
                    .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }

                value.last().extra.custom.value?.shard_hashes
                    ?.nodes()
                    .orEmpty()
                    .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }
                    .flatMap { curr ->
                        (lastMcShards.getOrDefault(curr.key, curr.value).seq_no + 1u..curr.value.seq_no)
                            .map {
                                TonNodeBlockId(
                                    curr.key,
                                    curr.value.next_validator_shard.toLong() /* Shard.ID_ALL */,
                                    it.toInt()
                                )
                            } // TODO
                    }
                    .mapNotNull {
                        try {
                            liteClient.lookupBlock(it)?.let { liteClient.getBlock(it) }
                        } catch (e: Exception) {
                            logger.warn("couldn't get block workchain={} seqno={}", it.workchain, it.seqno, e)
                            null
                        }
                    }
                    .plus(value)
            }
            .flatMapConcat { it.asFlow() }
            .onEach {
                logger.debug("block workchain={} seqno={}", it.info.shard.workchain_id, it.info.seq_no)
            }
            .shareIn(CoroutineScope(Dispatchers.Default), SharingStarted.Lazily, 4096)

    companion object : KLogging()
}

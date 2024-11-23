package ru.ushakov.billing.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BillingEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    val objectMapper: ObjectMapper
) {

    fun sendBillingReservedEvent(event: BillingReservedEvent) {
        kafkaTemplate.send("BillingReserved", objectMapper.writeValueAsString(event))
        println("BillingReservedEvent sent to Kafka: $event")
    }

    fun sendBillingFailedEvent(event: BillingFailedEvent) {
        kafkaTemplate.send("BillingFailed", objectMapper.writeValueAsString(event))
        println("BillingFailedEvent sent to Kafka: $event")
    }
}
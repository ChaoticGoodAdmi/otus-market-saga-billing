package ru.ushakov.billing.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.ushakov.billing.repository.AccountRepository
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BillingEventListener(
    private val accountRepository: AccountRepository,
    private val kafkaProducer: BillingEventProducer
) {

    @KafkaListener(topics = ["OrderCreated"], groupId = "billing-service-group")
    fun handleOrderCreatedEvent(message: String) {
        println("OrderCreatedEvent received with payload: $message")
        val event = parseOrderCreatedEvent(message)
        val account = accountRepository.findByAccountNumber(event.accountNumber)
        if (account == null) {
            kafkaProducer.sendBillingFailedEvent(
                BillingFailedEvent(orderId = event.orderId, reason = "Account not found")
            )
            return
        }

        if (account.balance < event.totalPrice) {
            kafkaProducer.sendBillingFailedEvent(
                BillingFailedEvent(orderId = event.orderId, reason = "Insufficient funds")
            )
            return
        }

        account.balance = account.balance.subtract(event.totalPrice)
        accountRepository.save(account)

        kafkaProducer.sendBillingReservedEvent(
            BillingReservedEvent(
                orderId = event.orderId,
                items = event.items,
                accountNumber = event.accountNumber,
                totalPrice = event.totalPrice,
                deliveryAddress = event.deliveryAddress,
                deliveryDate = event.deliveryDate
            )
        )
        println("Funds reserved successfully for order ID: ${event.orderId}")
    }

    @KafkaListener(topics = ["ItemReserveFailed"], groupId = "billing-service-group")
    fun handleItemReserveFailedEvent(message: String) {
        println("ItemReserveFailedEvent received with payload: $message")
        val event = parseItemReserveFailedEvent(message)
        val account = accountRepository.findByAccountNumber(event.accountNumber)
            ?: throw IllegalStateException("Refund for ${event.accountNumber} with ${event.totalPrice} RUR was not successful")
        account.balance = account.balance.add(event.totalPrice)
        accountRepository.save(account)
        println("Funds refunded successfully for account: ${event.accountNumber}")
    }

    private fun parseOrderCreatedEvent(message: String): OrderCreatedEvent {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        return mapper.readValue(message, OrderCreatedEvent::class.java)
    }

    private fun parseItemReserveFailedEvent(message: String): ItemReserveFailedEvent {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        return mapper.readValue(message, ItemReserveFailedEvent::class.java)
    }
}

data class OrderCreatedEvent(
    val orderId: Long,
    val items: List<Item>,
    val totalPrice: BigDecimal,
    val accountNumber: String,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

data class Item(
    val name: String,
    val price: BigDecimal,
    val quantity: Int
)

data class BillingFailedEvent(
    val orderId: Long,
    val reason: String
)

data class BillingReservedEvent(
    val orderId: Long,
    val items: List<Item>,
    val accountNumber: String,
    val totalPrice: BigDecimal,
    val deliveryAddress: String,
    val deliveryDate: LocalDate
)

data class ItemReserveFailedEvent(
    val orderId: Long,
    val accountNumber: String,
    val totalPrice: BigDecimal
)

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    }
}
package ru.ushakov.billing.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ushakov.billing.repository.AccountRepository
import ru.ushakov.billing.service.AccountService
import java.math.BigDecimal

@RestController
@RequestMapping("/billing")
class BillingController(
    private val accountService: AccountService
) {

    @PostMapping("/")
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Any> {
        try {
            accountService.saveAccount(request.accountNumber)
        } catch (ex: IllegalStateException) {
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Account with number ${request.accountNumber} already exists."))
        }
        return ResponseEntity.ok(mapOf("message" to "Account created successfully with number: ${request.accountNumber}"))
    }

    @GetMapping("/{accountNumber}")
    fun getAccount(@PathVariable accountNumber: String): ResponseEntity<Any> {
        val account = accountService.getAccount(accountNumber)
        return if (account != null) {
            ResponseEntity.ok(account)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/deposit")
    fun deposit(
        @RequestHeader("X-Request-ID") transactionKey: String,
        @RequestBody request: FundsExchangeRequest
    ): ResponseEntity<Any> {
        return try {
            val result = accountService.deposit(request.accountNumber, request.amount, transactionKey)
            ResponseEntity.ok(mapOf("balance" to result))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        }
    }

    @PostMapping("/withdraw")
    fun withdraw(
        @RequestHeader("X-Request-ID") transactionKey: String,
        @RequestBody request: FundsExchangeRequest
    ): ResponseEntity<Any> {
        return try {
            val result = accountService.withdraw(request.accountNumber, request.amount, transactionKey)
            ResponseEntity.ok(mapOf("balance" to result))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        }
    }
}

data class CreateAccountRequest(
    val accountNumber: String
)

data class FundsExchangeRequest(
    val accountNumber: String,
    val amount: BigDecimal
)
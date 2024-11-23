package ru.ushakov.billing.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ushakov.billing.domain.Account
import ru.ushakov.billing.repository.AccountRepository
import java.math.BigDecimal

@RestController
@RequestMapping("/billing")
class BillingController(
    private val accountRepository: AccountRepository
) {

    @PostMapping("/")
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Any> {
        val accountNumber = request.accountNumber
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Account with number $accountNumber already exists."))
        }

        val account = Account(accountNumber = accountNumber, balance = BigDecimal.ZERO)
        accountRepository.save(account)
        return ResponseEntity.ok(mapOf("message" to "Account created successfully with number: $accountNumber"))
    }

    @GetMapping("/{accountNumber}")
    fun getAccount(@PathVariable accountNumber: String): ResponseEntity<Any> {
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: return ResponseEntity.status(404).body(mapOf("message" to "Account with number $accountNumber not found."))
        return ResponseEntity.ok(account)
    }

    @PostMapping("/deposit")
    fun deposit(@RequestBody request: FundsExchangeRequest): ResponseEntity<Any> {
        val accountNumber = request.accountNumber
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: return ResponseEntity.status(404).body(mapOf("message" to "Account with number $accountNumber not found."))

        account.balance = account.balance.add(request.amount)
        accountRepository.save(account)
        return ResponseEntity.ok(mapOf("balance" to account.balance))
    }

    @PostMapping("/withdraw")
    fun withdraw(@RequestBody request: FundsExchangeRequest): ResponseEntity<Any> {
        val accountNumber = request.accountNumber
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: return ResponseEntity.status(404).body(mapOf("message" to "Account with number $accountNumber not found."))

        if (account.balance < request.amount) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Insufficient balance. Current balance: ${account.balance}"))
        }

        account.balance = account.balance.subtract(request.amount)
        accountRepository.save(account)
        return ResponseEntity.ok(mapOf("balance" to account.balance))
    }
}

data class CreateAccountRequest(
    val accountNumber: String
)

data class FundsExchangeRequest(
    val accountNumber: String,
    val amount: BigDecimal
)
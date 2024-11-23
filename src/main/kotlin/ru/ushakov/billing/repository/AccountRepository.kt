package ru.ushakov.billing.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.ushakov.billing.domain.Account

interface AccountRepository : JpaRepository<Account, Long> {
    fun findByAccountNumber(accountNumber: String): Account?
    fun existsByAccountNumber(accountNumber: String): Boolean
}
package ru.ushakov.billing.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.ushakov.billing.domain.TransactionRecord

interface TransactionRecordRepository : JpaRepository<TransactionRecord, String> {
    fun findByTransactionKey(transactionKey: String): TransactionRecord?
}
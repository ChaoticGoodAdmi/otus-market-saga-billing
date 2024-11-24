package ru.ushakov.billing.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "transaction_records")
class TransactionRecord (

    @Id
    @Column(nullable = false, unique = true)
    val transactionKey: String,

    @Column(nullable = false)
    val accountNumber: String,

    @Column(nullable = false)
    val operation: Operation,

    @Column(nullable = false)
    val resultBalance: BigDecimal
)

enum class Operation {
    DEPOSIT, WITHDRAW
}

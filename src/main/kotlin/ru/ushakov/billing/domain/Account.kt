package ru.ushakov.billing.domain

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val accountNumber: String,
    var balance: BigDecimal
)
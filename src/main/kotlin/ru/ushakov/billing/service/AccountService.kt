package ru.ushakov.billing.service

import org.springframework.stereotype.Service
import ru.ushakov.billing.domain.Account
import ru.ushakov.billing.domain.Operation
import ru.ushakov.billing.domain.TransactionRecord
import ru.ushakov.billing.repository.AccountRepository
import ru.ushakov.billing.repository.TransactionRecordRepository
import java.math.BigDecimal

@Service
class AccountService(
    private val transactionRecordRepository: TransactionRecordRepository,
    private val accountRepository: AccountRepository
) {

    fun deposit(accountNumber: String, amount: BigDecimal, transactionKey: String): BigDecimal {
        val existingRecord = transactionRecordRepository.findByTransactionKey(transactionKey)
        if (existingRecord != null) {
            return existingRecord.resultBalance
        }

        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: throw IllegalArgumentException("Account with number $accountNumber not found.")

        account.balance = account.balance.add(amount)
        saveTransactionRecordForOperation(account, transactionKey, accountNumber, Operation.DEPOSIT)

        return account.balance
    }

    fun withdraw(accountNumber: String, amount: BigDecimal, transactionKey: String): BigDecimal {
        val existingRecord = transactionRecordRepository.findByTransactionKey(transactionKey)
        if (existingRecord != null) {
            return existingRecord.resultBalance
        }

        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: throw IllegalArgumentException("Account with number $accountNumber not found.")

        require(account.balance >= amount) { "Insufficient balance. Current balance: ${account.balance}" }

        account.balance = account.balance.subtract(amount)
        saveTransactionRecordForOperation(account, transactionKey, accountNumber, Operation.WITHDRAW)

        return account.balance
    }

    private fun saveTransactionRecordForOperation(
        account: Account,
        transactionKey: String,
        accountNumber: String,
        operation: Operation
    ) {
        accountRepository.save(account)

        val record = TransactionRecord(
            transactionKey = transactionKey,
            accountNumber = accountNumber,
            operation = operation,
            resultBalance = account.balance
        )
        transactionRecordRepository.save(record)
    }

    fun saveAccount(accountNumber: String) {
        check(!accountRepository.existsByAccountNumber(accountNumber)) { "Account with number $accountNumber already exists." }

        val account = Account(accountNumber = accountNumber, balance = BigDecimal.ZERO)
        accountRepository.save(account)
    }

    fun getAccount(accountNumber: String): Account? {
        return accountRepository.findByAccountNumber(accountNumber)
    }


}
package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import com.clouway.rules.DataStoreRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.sql.ResultSet
import java.time.LocalDateTime

class JdbcTransactionRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    @Test
    fun getTransactionThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user", "password")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        val transactionCreatedOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val transaction = Transaction(userId, accountId, transactionCreatedOn, Operation.DEPOSIT, 5f)
        assertThat(dataStoreRule.transactionRepository.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(dataStoreRule.transactionRepository.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToRegisterTransactionBoundedToUserIdThatDoesNotExists() {
        assertThat(dataStoreRule.transactionRepository.registerTransaction(Transaction(1, 1,
                LocalDateTime.now(), Operation.DEPOSIT, 5f)), `is`(equalTo(false)))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(dataStoreRule.transactionRepository.getTransactions(1), `is`(equalTo(emptyList())))
    }

    @Test
    fun deleteTransactionThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user", "password")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        val transactionCreatedOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val transaction = Transaction(userId, accountId, transactionCreatedOn, Operation.DEPOSIT, 5f)
        dataStoreRule.transactionRepository.registerTransaction(transaction)
        assertThat(dataStoreRule.transactionRepository.deleteTransaction(transaction),
                `is`(equalTo(true)))
        assertThat(dataStoreRule.transactionRepository.getTransactions(userId), `is`(equalTo(emptyList())))
    }

    @Test
    fun tryToDeleteUnregisteredTransaction() {
        val transaction = Transaction(-1, -1, LocalDateTime.MIN, Operation.DEPOSIT, 5f)
        assertThat(dataStoreRule.transactionRepository.deleteTransaction(transaction), `is`(equalTo(false)))
    }
}
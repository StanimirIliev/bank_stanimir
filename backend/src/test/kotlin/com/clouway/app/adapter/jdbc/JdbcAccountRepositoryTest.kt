package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.Transaction
import com.clouway.app.core.TransactionRepository
import com.clouway.rules.DataStoreRule
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class JdbcAccountRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()


    @Test
    fun tryToRegisterAccountForUnregisteredUserId() {
        assertThat(dataStoreRule.accountRepository.registerAccount(Account("some fund",
                1, Currency.EUR, 0f)), `is`(equalTo(-1)))
    }

    @Test
    fun makeADeposit() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(dataStoreRule.accountRepository.updateBalance(accountId, userId, 30f).isSuccessful, `is`(equalTo(true)))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun makeAValidWithdraw() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        assertThat(dataStoreRule.accountRepository.updateBalance(accountId, userId, -20f).isSuccessful, `is`(equalTo(true)))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(dataStoreRule.accountRepository.updateBalance(accountId, userId, -20f).isSuccessful, `is`(equalTo(false)))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(0f)))
    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        assertThat(dataStoreRule.accountRepository.updateBalance(-1, -1, 20f).isSuccessful, `is`(equalTo(false)))
    }

    @Test
    fun checkIfTransactionRepositoryIsCalledOnUpdateBalance() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")

        val mockTransactionRepository = object : TransactionRepository {
            override fun registerTransaction(transaction: Transaction): Boolean {
                assertThat(transaction.userId, `is`(equalTo(userId)))
                return true
            }

            override fun getTransactions(userId: Int): List<Transaction> = emptyList()

            override fun deleteTransaction(transaction: Transaction): Boolean = false
        }
        val fakeAccountRepository = JdbcAccountRepository(
                MySQLJdbcTemplate(dataStoreRule.mySqlDataSource),
                mockTransactionRepository
        )
        val accountId = fakeAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        fakeAccountRepository.updateBalance(accountId, userId, 30f)
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        dataStoreRule.accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(dataStoreRule.accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)), `is`(equalTo(-1)))
    }

    @Test
    fun getAllAccountsByUserId() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val account1 = Account("Fund for something", userId, Currency.BGN, 0f)
        val account2 = Account("Fund for other something", userId, Currency.BGN, 0f)
        val accountId1 = dataStoreRule.accountRepository.registerAccount(account1)
        val accountId2 = dataStoreRule.accountRepository.registerAccount(account2)
        assertThat(dataStoreRule.accountRepository.getAllAccounts(userId), `is`(equalTo(listOf(
                account2.apply { id = accountId2 },
                account1.apply { id = accountId1 }
        ))))
    }

    @Test
    fun tryToGetAllAccountsByUnregisteredUserId() {
        assertThat(dataStoreRule.accountRepository.getAllAccounts(1), `is`(equalTo(emptyList())))
    }

    @Test
    fun getAccountThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = dataStoreRule.accountRepository.registerAccount(account)
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        assertThat(dataStoreRule.accountRepository.getUserAccount(1, 1), `is`(nullValue()))
    }

    @Test
    fun tryToGetRegisteredAccountOfOtherUser() {
        val userId1 = dataStoreRule.userRepository.registerUser("user1", "password")
        val userId2 = dataStoreRule.userRepository.registerUser("user2", "password")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("Some fund", userId1, Currency.BGN, 0f))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId2, accountId), `is`(nullValue()))
    }

    @Test
    fun removeAccountThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password123")
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(dataStoreRule.accountRepository.removeAccount(accountId, userId).isSuccessful, `is`(equalTo(true)))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId), `is`(nullValue()))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        assertThat(dataStoreRule.accountRepository.removeAccount(-1, -1).isSuccessful, `is`(equalTo(false)))
    }
}

package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader

class JdbcAccountRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val accountsTable = "Accounts"
    private val transactionTable = "Transactions"
    private val userTable = "Users"
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var mySqlJdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        mySqlJdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
        transactionRepository = JdbcTransactionRepository(mySqlJdbcTemplate, transactionTable)
        accountRepository = JdbcAccountRepository(mySqlJdbcTemplate, transactionRepository, accountsTable)
        userRepository = JdbcUserRepository(mySqlJdbcTemplate, userTable)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the accountsTable exists
            statement.execute("DELETE FROM $userTable")//   clears accountsTable
        } else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the accountsTable does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$accountsTable'").next()) {//  check if the accountsTable exists
            statement.execute("DELETE FROM $accountsTable")//   clears accountsTable
        } else {
            statement.execute(FileReader("schema/$accountsTable.sql").readText())// if the accountsTable does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$transactionTable'").next()) {//  check if the accountsTable exists
            statement.execute("DELETE FROM $transactionTable")//   clears accountsTable
        } else {
            statement.execute(FileReader("schema/$transactionTable.sql").readText())// if the accountsTable does not exists create it
        }
    }

    @Test
    fun tryToRegisterAccountForUnregisteredUserId() {
        assertThat(accountRepository.registerAccount(Account("some fund",
                1, Currency.EUR, 0f)), `is`(equalTo(-1)))
    }

    @Test
    fun makeADeposit() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(accountRepository.updateBalance(accountId, userId, 30f).successful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun makeAValidWithdraw() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        assertThat(accountRepository.updateBalance(accountId, userId, -20f).successful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(accountRepository.updateBalance(accountId, userId, -20f).successful, `is`(equalTo(false)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(0f)))
    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        assertThat(accountRepository.updateBalance(-1, -1, 20f).successful, `is`(equalTo(false)))
    }

    @Test
    fun checkIfTransactionRepositoryIsCalledOnUpdateBalance() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")

        val mockTransactionRepository = object : TransactionRepository {
            override fun registerTransaction(transaction: Transaction): Boolean {
                assertThat(transaction.userId, `is`(equalTo(userId)))
                return true
            }

            override fun getTransactions(userId: Int): List<Transaction> = emptyList()
        }
        val fakeAccountRepository = JdbcAccountRepository(
                MySQLJdbcTemplate(mySqlDataSource),
                mockTransactionRepository,
                accountsTable
        )
        val accountId = fakeAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        fakeAccountRepository.updateBalance(accountId, userId, 30f)
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)), `is`(equalTo(-1)))
    }

    @Test
    fun getAllAccountsByUserId() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val account1 = Account("Fund for something", userId, Currency.BGN, 0f)
        val account2 = Account("Fund for other something", userId, Currency.BGN, 0f)
        val accountId1 = accountRepository.registerAccount(account1)
        val accountId2 = accountRepository.registerAccount(account2)
        assertThat(accountRepository.getAllAccounts(userId), `is`(equalTo(listOf(
                account2.apply { id = accountId2 },
                account1.apply { id = accountId1 }
        ))))
    }

    @Test
    fun tryToGetAllAccountsByUnregisteredUserId() {
        assertThat(accountRepository.getAllAccounts(1), `is`(equalTo(emptyList())))
    }

    @Test
    fun getAccountThatWasRegistered() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = accountRepository.registerAccount(account)
        assertThat(accountRepository.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        assertThat(accountRepository.getUserAccount(1, 1), `is`(nullValue()))
    }

    @Test
    fun tryToGetRegisteredAccountOfOtherUser() {
        userRepository.registerUser("user1", "password")
        userRepository.registerUser("user2", "password")
        val userId1 = getUserId("user1")
        val userId2 = getUserId("user2")
        val accountId = accountRepository.registerAccount(Account("Some fund", userId1, Currency.BGN, 0f))
        assertThat(accountRepository.getUserAccount(userId2, accountId), `is`(nullValue()))
    }

    @Test
    fun removeAccountThatWasRegistered() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val accountId = accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.removeAccount(accountId, userId).successful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId), `is`(nullValue()))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        assertThat(accountRepository.removeAccount(-1, -1).successful, `is`(equalTo(false)))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }
}

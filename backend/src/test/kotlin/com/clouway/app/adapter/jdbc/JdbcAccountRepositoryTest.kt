package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.sql.Timestamp

class JdbcAccountRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val table = "Accounts"
    private val transactionTable = "Transactions"
    private val userTable = "Users"
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        transactionRepository = JdbcTransactionRepository(MySQLJdbcTemplate(mySqlDataSource), transactionTable)
        accountRepository = JdbcAccountRepository(MySQLJdbcTemplate(mySqlDataSource), transactionRepository, table)
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource),userTable)
        val statement = mySqlDataSource.connection.createStatement()
        if(statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $userTable")//   clears table
        }
        else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the table does not exists create it
        }
        if(statement.executeQuery("SHOW TABLES LIKE '$table'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $table")//   clears table
        }
        else {
            statement.execute(FileReader("schema/$table.sql").readText())// if the table does not exists create it
        }
    }

    @Test
    fun getBalanceFromRegisteredAccount() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        assertThat(accountRepository.registerAccount(Account( "some fund",
                userId, Currency.BGN, 20f)),
                `is`(equalTo(true)))
        assertThat(accountRepository.getBalance(getAccountId("some fund", userId)), `is`(equalTo(20f)))
    }

    @Test
    fun tryToRegisterAccountForUnregisteredUserId() {
        assertThat(accountRepository.registerAccount(Account("some fund",
                1, Currency.EUR, 0f)),
                `is`(equalTo(false)))
    }

    @Test
    fun tryToGetBalanceFromUnregisteredAccountId() {
        assertThat(accountRepository.getBalance(1), `is`(nullValue()))
    }

    @Test
    fun makeADeposit() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("some fund", userId)
        assertThat(accountRepository.updateBalance(accountId, 30f), `is`(equalTo(true)))
        assertThat(accountRepository.getBalance(accountId), `is`(equalTo(30f)))
    }

    @Test
    fun makeAValidWithdraw() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        val accountId = getAccountId("some fund", userId)
        assertThat(accountRepository.updateBalance(accountId, -20f), `is`(equalTo(true)))

    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("some fund", userId)
        assertThat(accountRepository.updateBalance(accountId, -20f), `is`(equalTo(false)))

    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        assertThat(accountRepository.updateBalance(1, 20f), `is`(equalTo(false)))
    }

    @Test
    fun checkIfTransactionRepositoryIsCalledOnUpdateBalance() {
        var userId: Int = -1
        val mockTransactionRepository = object : TransactionRepository {
            override fun registerTransaction(transaction: Transaction): Boolean {
                assertThat(transaction.userId, `is`(equalTo(userId)))
                return true
            }

            override fun getTransactions(userId: Int): List<Transaction> {
                return emptyList()
            }

            override fun getTransactionId(userId: Int, onDate: Timestamp): Int? {
                return null
            }
        }
        val manipulatedAccountRepository = JdbcAccountRepository(MySQLJdbcTemplate(mySqlDataSource),mockTransactionRepository, table)
        userRepository.registerUser("user123", "password123")
        userId = getUserId("user123", "password123")
        manipulatedAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("some fund", userId)
        manipulatedAccountRepository.updateBalance(accountId, 30f)
    }

    @Test
    fun getTitleOfTheRegisteredAccount() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.getTitle(accountId), `is`(equalTo("Fund for something")))
    }

    @Test
    fun tryToGetTheTitleOfUnregisteredAccount() {
        assertThat(accountRepository.getTitle(1), `is`(nullValue()))
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)),
                `is`(equalTo(false)))
    }

    @Test
    fun getAccountIdOfRegisteredAccount() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.getAccountId("Fund for something", userId), `is`(equalTo(accountId)))
    }

    @Test
    fun tryToGetIdOfUnregisteredAccount() {
        assertThat(accountRepository.getAccountId("Fund for something", 1), `is`(nullValue()))
    }

    @Test
    fun getUserIdByItsAccountId() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123", "password123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.getUserId(accountId), `is`(equalTo(userId)))
    }

    @Test
    fun tryToGetUserIdWithUnregisteredAccountId() {
        assertThat(accountRepository.getUserId(1), `is`(nullValue()))
    }

    private fun getUserId(username: String, password: String): Int {
        return userRepository.getUserId(username, password) ?:
                throw IllegalArgumentException("Could not get user id")


    }

    private fun getAccountId(title: String, userId: Int): Int {
        return accountRepository.getAccountId(title, userId) ?:
                throw IllegalArgumentException("Could not get account id")
    }
}
package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
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
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), userTable)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $userTable")//   clears table
        } else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the table does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$table'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $table")//   clears table
        } else {
            statement.execute(FileReader("schema/$table.sql").readText())// if the table does not exists create it
        }
    }

    @Test
    fun getBalanceFromRegisteredAccount() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        assertThat(accountRepository.registerAccount(Account("some fund",
                userId, Currency.BGN, 20f)),
                `is`(equalTo(true)))
        assertThat(accountRepository.getBalance(getAccountId("some fund", userId)), `is`(equalTo(20f)))
    }

    @Test
    fun tryToGetBalanceFromUnregisteredAccount() {
        assertThat(accountRepository.getBalance(1), `is`(nullValue()))
    }

    @Test
    fun tryToRegisterAccountForUnregisteredUserId() {
        assertThat(accountRepository.registerAccount(Account("some fund",
                1, Currency.EUR, 0f)),
                `is`(equalTo(false)))
    }

    @Test
    fun tryToRegisterTwoAccountsWithTheSameName() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Same name", userId, Currency.BGN, 0f))
        accountRepository.registerAccount(Account("Same name", userId, Currency.EUR, 0f))
    }

    @Test
    fun makeADeposit() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("some fund", userId)
        assertThat(accountRepository.updateBalance(accountId, 30f), `is`(equalTo(true)))
        assertThat(accountRepository.getBalance(accountId), `is`(equalTo(30f)))
    }

    @Test
    fun makeAValidWithdraw() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        val accountId = getAccountId("some fund", userId)
        assertThat(accountRepository.updateBalance(accountId, -20f), `is`(equalTo(true)))

    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
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

            override fun getTransactionId(userId: Int, accountId: Int, onDate: Timestamp): Int {
                return -1
            }
        }
        val manipulatedAccountRepository = JdbcAccountRepository(MySQLJdbcTemplate(mySqlDataSource), mockTransactionRepository, table)
        userRepository.registerUser("user123", "password123")
        userId = getUserId("user123")
        manipulatedAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("some fund", userId)
        manipulatedAccountRepository.updateBalance(accountId, 30f)
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)),
                `is`(equalTo(false)))
    }

    @Test
    fun getAccountIdOfRegisteredAccount() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.getAccountId("Fund for something", userId), `is`(equalTo(accountId)))
    }

    @Test
    fun tryToGetIdOfUnregisteredAccount() {
        assertThat(accountRepository.getAccountId("Fund for something", 1), `is`(equalTo(-1)))
    }

    @Test
    fun getUserIdByItsAccountId() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.getUserId(accountId), `is`(equalTo(userId)))
    }

    @Test
    fun tryToGetUserIdWithUnregisteredAccountId() {
        assertThat(accountRepository.getUserId(1), `is`(equalTo(-1)))
    }

    @Test
    fun getAllAccountsByUserId() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        val firstAccount = Account("Fund for something", userId, Currency.BGN, 0f)
        val secondAccount = Account("Fund for other something", userId, Currency.BGN, 0f)
        accountRepository.registerAccount(firstAccount)
        accountRepository.registerAccount(secondAccount)
        assertThat(accountRepository.getAllAccounts(userId), `is`(equalTo(listOf(secondAccount, firstAccount))))
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
        accountRepository.registerAccount(account)
        val accountId = accountRepository.getAccountId("Fund for something", userId) ?:
                throw IllegalArgumentException("Could not get account with title: 'Fund for something' and\n" +
                        "userId: $userId")
        assertThat(accountRepository.getAccount(accountId), `is`(equalTo(account)))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        assertThat(accountRepository.getAccount(1), `is`(nullValue()))
    }

    @Test
    fun removeAccountThatWasRegistered() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)
        assertThat(accountRepository.removeAccount(accountId), `is`(equalTo(true)))
        assertThat(accountRepository.getAccount(accountId), `is`(nullValue()))
    }

    @Test
    fun authorizeAccountAndUser() {
        userRepository.registerUser("user123", "password123")
        val userId = getUserId("user123")
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Fund for something", userId)

        userRepository.registerUser("otherUser", "password")
        val otherUserId = getUserId("otherUser")

        assertThat(accountRepository.authenticate(accountId, otherUserId), `is`(equalTo(false)))
        assertThat(accountRepository.authenticate(accountId, userId), `is`(equalTo(true)))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        assertThat(accountRepository.removeAccount(1), `is`(equalTo(false)))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }

    private fun getAccountId(title: String, userId: Int): Int {
        return accountRepository.getAccountId(title, userId)
    }
}

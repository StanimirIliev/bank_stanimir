package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.Operation
import com.clouway.app.core.Transaction
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.sql.Timestamp
import java.time.LocalDateTime

class JdbcTransactionRepositoryTest {

    private val mySqlDataSource = MysqlDataSource()

    private val table = "Transactions"
    private val userTable = "Users"
    private val accountTable = "Accounts"
    private lateinit var transactionRepository: JdbcTransactionRepository
    private lateinit var userRepository: JdbcUserRepository
    private lateinit var accountRepository: JdbcAccountRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        transactionRepository = JdbcTransactionRepository(MySQLJdbcTemplate(mySqlDataSource), table)
        userRepository = JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), userTable)
        accountRepository = JdbcAccountRepository(MySQLJdbcTemplate(mySqlDataSource), transactionRepository, accountTable)
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
        if (statement.executeQuery("SHOW TABLES LIKE '$accountTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $accountTable")//   clears table
        } else {
            statement.execute(FileReader("schema/$accountTable.sql").readText())// if the table does not exists create it
        }
    }

    @Test
    fun getTransactionThatWasRegistered() {
        userRepository.registerUser("user", "password")
        val userId = getUserId("user")
        accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Some fund", userId)
        val transaction = Transaction(userId, accountId, Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)
        assertThat(transactionRepository.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(transactionRepository.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToRegisterTransactionBoundedToUserIdThatDoesNotExists() {
        assertThat(transactionRepository.registerTransaction(Transaction(1, 1,
                Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)), `is`(equalTo(false)))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(transactionRepository.getTransactions(1), `is`(equalTo(emptyList())))
    }

    @Test
    fun getIdOfRegisteredTransaction() {
        userRepository.registerUser("user", "password")
        val userId = getUserId("user")
        accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        val accountId = getAccountId("Some fund", userId)
        val transaction = Transaction(userId, accountId, Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)
        transactionRepository.registerTransaction(transaction)
        assertThat(transactionRepository.getTransactionId(transaction.userId, accountId, transaction.onDate),
                `is`(any(Int::class.java)))
    }

    @Test
    fun tryToGetIdOfUnregisteredTransaction() {
        assertThat(transactionRepository.getTransactionId(1, 1, Timestamp.valueOf(LocalDateTime.now())),
                `is`(equalTo(-1)))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }

    private fun getAccountId(title: String, userId: Int): Int {
        return accountRepository.getAccountId(title, userId)
    }
}
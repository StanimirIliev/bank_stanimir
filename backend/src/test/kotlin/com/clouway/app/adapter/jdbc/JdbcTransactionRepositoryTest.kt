package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.time.LocalDateTime

class JdbcTransactionRepositoryTest {

    private val mySqlDataSource = MysqlDataSource()

    private val transactionTable = "Transactions"
    private val userTable = "Users"
    private val accountTable = "Accounts"
    private lateinit var transactionRepository: JdbcTransactionRepository
    private lateinit var userRepository: JdbcUserRepository
    private lateinit var accountRepository: JdbcAccountRepository
    private lateinit var mySqlJdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        mySqlJdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
        transactionRepository = JdbcTransactionRepository(mySqlJdbcTemplate, transactionTable)
        userRepository = JdbcUserRepository(mySqlJdbcTemplate, userTable)
        accountRepository = JdbcAccountRepository(mySqlJdbcTemplate, transactionRepository, accountTable)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the transactionTable exists
            statement.execute("DELETE FROM $userTable")//   clears transactionTable
        } else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the transactionTable does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$transactionTable'").next()) {//  check if the transactionTable exists
            statement.execute("DELETE FROM $transactionTable")//   clears transactionTable
        } else {
            statement.execute(FileReader("schema/$transactionTable.sql").readText())// if the transactionTable does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$accountTable'").next()) {//  check if the transactionTable exists
            statement.execute("DELETE FROM $accountTable")//   clears transactionTable
        } else {
            statement.execute(FileReader("schema/$accountTable.sql").readText())// if the transactionTable does not exists create it
        }
    }

    @Test
    fun getTransactionThatWasRegistered() {
        userRepository.registerUser("user", "password")
        val userId = getUserId("user")
        val accountId = accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        val transactionCreatedOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val transaction = Transaction(userId, accountId, transactionCreatedOn, Operation.DEPOSIT, 5f)
        assertThat(transactionRepository.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(transactionRepository.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToRegisterTransactionBoundedToUserIdThatDoesNotExists() {
        assertThat(transactionRepository.registerTransaction(Transaction(1, 1,
                LocalDateTime.now(), Operation.DEPOSIT, 5f)), `is`(equalTo(false)))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(transactionRepository.getTransactions(1), `is`(equalTo(emptyList())))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }
}
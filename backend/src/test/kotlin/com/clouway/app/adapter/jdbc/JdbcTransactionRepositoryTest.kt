package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
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
    private lateinit var transactionRepository: JdbcTransactionRepository
    private lateinit var userRepository: JdbcUserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        transactionRepository = JdbcTransactionRepository(MySQLJdbcTemplate(mySqlDataSource), table)
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
    fun getTransactionThatWasRegistered() {
        userRepository.registerUser("user", "password")
        val userId = getUserId("user", "password")
        val transaction = Transaction(userId, Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)
        assertThat(transactionRepository.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(transactionRepository.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToRegisterTransactionBoundedToUserIdThatDoesNotExists() {
        assertThat(transactionRepository.registerTransaction(Transaction(1,
                Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)), `is`(equalTo(false)))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(transactionRepository.getTransactions(1), `is`(equalTo(emptyList())))
    }

    @Test
    fun getIdOfRegisteredTransaction() {
        userRepository.registerUser("user", "password")
        val userId = getUserId("user", "password")
        val transaction = Transaction(userId, Timestamp.valueOf(LocalDateTime.now()), Operation.DEPOSIT, 5f)
        transactionRepository.registerTransaction(transaction)
        assertThat(transactionRepository.getTransactionId(transaction.userId, transaction.onDate),
                `is`(any(Int::class.java)))
    }

    @Test
    fun tryToGetIdOfUnregisteredTransaction() {
        assertThat(transactionRepository.getTransactionId(1, Timestamp.valueOf(LocalDateTime.now())), `is`(nullValue()))
    }

    private fun getUserId(username: String, password: String): Int {
        return userRepository.getUserId(username, password) ?:
                throw IllegalArgumentException("Could not get user id")


    }
}
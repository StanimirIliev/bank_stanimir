package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.Session
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.sql.Timestamp
import java.time.LocalDateTime

class JdbcSessionRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val table = "Sessions"
    private val userTable = "Users"
    private lateinit var sessionRepository: JdbcSessionRepository
    private lateinit var userRepository: JdbcUserRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        sessionRepository = JdbcSessionRepository(MySQLJdbcTemplate(mySqlDataSource), table, userTable)
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
    fun getSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val session = Session("hdwasd", userId, expiresAt)
        assertThat(sessionRepository.registerSession(session),
                `is`(equalTo(true)))
        assertThat(sessionRepository.getSession("hdwasd"), `is`(equalTo(session)))
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val session = Session("hdwasd", 1, Timestamp.valueOf(LocalDateTime.now()))
        assertThat(sessionRepository.registerSession(session),
                `is`(equalTo(false)))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        assertThat(sessionRepository.getSession("notExistingId"), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().minusHours(1))
        sessionRepository.registerSession(Session("hdwasd", userId, expiresAt))
        assertThat(sessionRepository.getSession("hdwasd"), `is`(nullValue()))
    }

    @Test
    fun tryToRegisterTwoSessionsWithTheSameId() {
        userRepository.registerUser("user123", "somePassword")
        val firstUserId = getUserId("user123", "somePassword")
        userRepository.registerUser("anotherUser", "somePassword")
        val secondUserId = getUserId("anotherUser", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(1))
        sessionRepository.registerSession(Session("hdwasd", firstUserId, expiresAt))
        assertThat(sessionRepository.registerSession(Session("hdwasd", secondUserId, expiresAt)),
                `is`(equalTo(false)))
    }

    @Test
    fun getActiveSessionsCount() {
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(0)))
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        sessionRepository.registerSession(Session("hdwasd", userId, expiresAt))
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(1)))
    }

    @Test
    fun deleteSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123", "somePassword")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        sessionRepository.registerSession(Session("hdwasd", userId, expiresAt))
        assertThat(sessionRepository.deleteSession("hdwasd"), `is`(equalTo(true)))
    }

    @Test
    fun tryToDeleteSessionThatWasNotRegistered() {
        assertThat(sessionRepository.deleteSession("notExistingId"), `is`(equalTo(false)))
    }

    private fun getUserId(username: String, password: String): Int {
        return userRepository.getUserId(username, password) ?:
                throw IllegalArgumentException("Could not get user id")


    }
}

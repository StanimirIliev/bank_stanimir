package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.Session
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Assert.fail
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
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val session = Session(userId, expiresAt)
        val sessionId = sessionRepository.registerSession(session)
        assertThat(sessionRepository.getSession(sessionId!!), `is`(equalTo(session)))
    }

    @Test
    fun getSessionIdOfRegisteredSession() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val sessionId = sessionRepository.registerSession(Session(userId, expiresAt))!!
        assertThat(sessionRepository.getSessionId(userId, expiresAt), `is`(equalTo(sessionId)))
    }

    @Test
    fun tryToGetSessionIdOfUnregisteredSession() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val wrongTimestamp = Timestamp.valueOf(LocalDateTime.now().plusHours(1))
        val sessionId = sessionRepository.registerSession(Session(userId, wrongTimestamp))!!
        assertThat(sessionRepository.getSessionId(userId, expiresAt.apply{}), `is`(nullValue()))
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val session = Session(1, Timestamp.valueOf(LocalDateTime.now()))
        assertThat(sessionRepository.registerSession(session), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        assertThat(sessionRepository.getSession("notExistingId"), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().minusHours(1))
        val sessionId = sessionRepository.registerSession(Session(userId, expiresAt))
        assertThat(sessionRepository.getSession(sessionId!!), `is`(nullValue()))
    }

    @Test
    fun getActiveSessionsCount() {
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(0)))
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        sessionRepository.registerSession(Session(userId, expiresAt))
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(1)))
    }

    @Test
    fun deleteSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val expiresAt = Timestamp.valueOf(LocalDateTime.now().plusHours(2))
        val sessionId = sessionRepository.registerSession(Session(userId, expiresAt))
        assertThat(sessionRepository.deleteSession(sessionId!!), `is`(equalTo(true)))
        assertThat(sessionRepository.getSession(sessionId), `is`(nullValue()))
    }

    @Test
    fun tryToDeleteSessionThatWasNotRegistered() {
        assertThat(sessionRepository.deleteSession("notExistingId"), `is`(equalTo(false)))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }
}

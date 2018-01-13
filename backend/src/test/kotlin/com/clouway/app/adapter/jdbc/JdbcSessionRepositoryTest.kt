package com.clouway.app.adapter.jdbc

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.Session
import com.mysql.cj.jdbc.MysqlDataSource
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.time.LocalDateTime

class JdbcSessionRepositoryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val sessionTable = "Sessions"
    private val userTable = "Users"
    private lateinit var sessionRepository: JdbcSessionRepository
    private lateinit var userRepository: JdbcUserRepository
    private lateinit var mySqlJdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        mySqlJdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
        sessionRepository = JdbcSessionRepository(mySqlJdbcTemplate, sessionTable, userTable)
        userRepository = JdbcUserRepository(mySqlJdbcTemplate, userTable)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $userTable")//   clears table
        } else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the table does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$sessionTable'").next()) {//  check if the table exists
            statement.execute("DELETE FROM $sessionTable")//   clears table
        } else {
            statement.execute(FileReader("schema/$sessionTable.sql").readText())// if the table does not exists create it
        }
    }

    @Test
    fun getSessionThatWasRegistered() {
        val userId = userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessionRepository.registerSession(session)
        assertThat(sessionRepository.getSessionAvailableAt(sessionId!!, instant), `is`(equalTo(session)))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        //TODO
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val session = Session(1, createdOn, expiresAt)
        assertThat(sessionRepository.registerSession(session), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionAvailableAt("notExistingId", instant), `is`(nullValue()))
    }

    @Test
    fun getSessionsCount() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val userId = userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun getSessionsCountOfOnlyNotExpiredSessions() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val userId = userRepository.registerUser("user123", "somePassword")
        val createdOn1 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt1 = LocalDateTime.of(2018, 1, 12, 15, 10)
        val createdOn2 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt2 = LocalDateTime.of(2018, 1, 12, 14, 15)
        sessionRepository.registerSession(Session(userId, createdOn1, expiresAt1))
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun terminateSessionThatWasRegistered() {
        val userId = userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val sessionId = sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(sessionRepository.terminateSession(sessionId!!), `is`(equalTo(true)))
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateSessionThatWasNotRegistered() {
        assertThat(sessionRepository.terminateSession("notExistingId"), `is`(equalTo(false)))
    }
}

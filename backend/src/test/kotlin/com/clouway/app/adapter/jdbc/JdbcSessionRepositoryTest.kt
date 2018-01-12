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
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val session = Session(userId, createdOn)
        val sessionId = sessionRepository.registerSession(session)
        assertThat(sessionRepository.getSession(sessionId!!), `is`(equalTo(session)))
    }

    @Test
    fun getSessionIdOfRegisteredSession() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val sessionId = sessionRepository.registerSession(Session(userId, createdOn))!!
        assertThat(sessionRepository.getSessionId(userId, createdOn), `is`(equalTo(sessionId)))
    }

    @Test
    fun tryToGetSessionIdWithWrongCreationTimeParameter() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val wrongCreationTime = LocalDateTime.of(2010, 1, 1, 0, 0)
        sessionRepository.registerSession(Session(userId, createdOn))!!
        assertThat(sessionRepository.getSessionId(userId, wrongCreationTime), `is`(nullValue()))
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val session = Session(1, createdOn)
        assertThat(sessionRepository.registerSession(session), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        assertThat(sessionRepository.getSession("notExistingId"), `is`(nullValue()))
    }

    @Test
    fun getSessionsCount() {
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(0)))
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        sessionRepository.registerSession(Session(userId, createdOn))
        assertThat(sessionRepository.getSessionsCount(), `is`(equalTo(1)))
    }

    @Test
    fun terminateSessionThatWasRegistered() {
        userRepository.registerUser("user123", "somePassword")
        val userId = getUserId("user123")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val sessionId = sessionRepository.registerSession(Session(userId, createdOn))
        assertThat(sessionRepository.terminateSession(sessionId!!), `is`(equalTo(true)))
        assertThat(sessionRepository.getSession(sessionId), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateSessionThatWasNotRegistered() {
        assertThat(sessionRepository.terminateSession("notExistingId"), `is`(equalTo(false)))
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }
}

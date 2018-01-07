package com.clouway.app.restservices.get

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.http.get.UsernameRoute
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import com.mysql.cj.jdbc.MysqlDataSource
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import spark.Spark.*
import java.io.FileReader
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.LocalDateTime


class UsernameQueryTest {
    private val mySqlDataSource = MysqlDataSource()

    private val port = 8080
    private val table = "Sessions"
    private val userTable = "Users"
    private val domain = "127.0.0.1"
    private lateinit var sessionRepository: SessionRepository
    private lateinit var userRepository: UserRepository

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
        port(port)
        get("/v1/username", UsernameRoute(
                sessionRepository,
                userRepository,
                Logger.getLogger(UsernameQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getUsernameAsRegisteredUser() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
        val request = HttpGet("http://$domain:$port/v1/username")
        val response = client.execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"username\":\"user123\"}")))
    }

    private fun createSessionAndCookie(username: String, password: String): CookieStore {
        fun getUserId(username: String): Int {
            return userRepository.getUserId(username)
        }

        userRepository.registerUser(username, password)
        val userId = getUserId(username)
        val sessionId = sessionRepository.registerSession(
                Session(userId, Timestamp.valueOf(LocalDateTime.now().plusHours(2)))) ?:
                throw Exception("Unable to register the session")
        val cookieStore = BasicCookieStore()
        val cookie = BasicClientCookie("sessionId", sessionId)
        cookie.domain = domain
        cookie.path = "/"
        cookieStore.addCookie(cookie)
        return cookieStore
    }
}

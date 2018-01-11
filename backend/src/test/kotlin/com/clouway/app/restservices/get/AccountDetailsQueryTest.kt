package com.clouway.app.restservices.get

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.http.get.AccountDetailsRoute
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.*
import com.google.gson.Gson
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

class AccountDetailsQueryTest {

    private val mySqlDataSource = MysqlDataSource()

    private val port = 8080
    private val accountsTable = "Accounts"
    private val sessionTable = "Sessions"
    private val userTable = "Users"
    private val transactionsTable = "Transactions"
    private val domain = "127.0.0.1"
    private lateinit var sessionRepository: SessionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository

    @Before
    fun setUp() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        val jdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
        sessionRepository = JdbcSessionRepository(jdbcTemplate, sessionTable, userTable)
        userRepository = JdbcUserRepository(jdbcTemplate, userTable)
        transactionRepository = JdbcTransactionRepository(jdbcTemplate, transactionsTable)
        accountRepository = JdbcAccountRepository(jdbcTemplate, transactionRepository, accountsTable)
        val statement = mySqlDataSource.connection.createStatement()
        if (statement.executeQuery("SHOW TABLES LIKE '$userTable'").next()) {//  check if the accountsTable exists
            statement.execute("DELETE FROM $userTable")//   clears accountsTable
        } else {
            statement.execute(FileReader("schema/$userTable.sql").readText())// if the accountsTable does not exists create it
        }
        if (statement.executeQuery("SHOW TABLES LIKE '$accountsTable'").next()) {//  check if the accountsTable exists
            statement.execute("DELETE FROM $accountsTable")//   clears accountsTable
        } else {
            statement.execute(FileReader("schema/$accountsTable.sql").readText())// if the accountsTable does not exists create it
        }
        port(port)
        get("/v1/account", AccountDetailsRoute(
                sessionRepository,
                accountRepository,
                Logger.getLogger(AccountDetailsQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getAccountDetailsAsAuthorizedUser() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val userId = getUserId("user123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val accountJson = Gson().toJson(account.apply { id = accountId })
        val request = HttpGet("http://$domain:$port/v1/account?id=$accountId")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"account\":$accountJson}")))
    }

    @Test
    fun tryToGetAccountDetailsAsUnauthorizedUser() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        userRepository.registerUser("otherUser", "password123")
        val userId = getUserId("otherUser")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val request = HttpGet("http://$domain:$port/v1/account?id=$accountId")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.UNAUTHORIZED_401)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Cannot view details for this account. Access denied.\"}")))
    }

    @Test
    fun tryToGetAccountDetailsWithIdThatDoesNotExist() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val request = HttpGet("http://$domain:$port/v1/account?id=-1")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Account not found.\"}")))
    }

    @Test
    fun tryToGetAccountDetailsWithoutPassingAnId() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val request = HttpGet("http://$domain:$port/v1/account")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Cannot get account. No account id passed with the request.\"}")))
    }

    private fun getAccountId(title: String, userId: Int): Int {
        return accountRepository.getAccountId(title, userId)
    }

    private fun getUserId(username: String): Int {
        return userRepository.getUserId(username)
    }

    private fun createSessionAndCookie(username: String, password: String): CookieStore {
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

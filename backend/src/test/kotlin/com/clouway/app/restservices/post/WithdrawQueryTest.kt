package com.clouway.app.restservices.post

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.http.post.WithdrawRoute
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
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

class WithdrawQueryTest {
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
        post("/v1/executeWithdraw", WithdrawRoute(
                sessionRepository,
                accountRepository,
                Logger.getLogger(WithdrawQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun executeWithdrawAsAuthorizedUser() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val userId = getUserId("user123")
        val account = Account("Fund for something", userId, Currency.BGN, 50f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val request = HttpPost("http://$domain:$port/v1/executeWithdraw")
        val params = StringEntity("{\"params\":{\"id\":$accountId,\"value\":30}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.CREATED_201)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Withdraw successful.\"}")))
        assertThat(accountRepository.getAccount(accountId)!!.balance, `is`(equalTo(20f)))
    }

    @Test
    fun tryToExecuteWithdrawBiggerThanBalance() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val userId = getUserId("user123")
        val account = Account("Fund for something", userId, Currency.BGN, 20f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val request = HttpPost("http://$domain:$port/v1/executeWithdraw")
        val params = StringEntity("{\"params\":{\"id\":$accountId,\"value\":30}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Withdraw unsuccessful. Not enough balance.\"}")))
        assertThat(accountRepository.getAccount(accountId)!!.balance, `is`(equalTo(20f)))
    }

    @Test
    fun tryToExecuteWithdrawAsUnauthorizedUser() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        userRepository.registerUser("otherUser", "password123")
        val userId = getUserId("otherUser")
        val account = Account("Fund for something", userId, Currency.BGN, 100f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val request = HttpPost("http://$domain:$port/v1/executeWithdraw")
        val params = StringEntity("{\"params\":{\"id\":$accountId,\"value\":50}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Cannot execute this withdraw. Access denied.\"}")))
        assertThat(accountRepository.getAccount(accountId)!!.balance, `is`(equalTo(100f)))
    }

    @Test
    fun tryToExecuteWithdrawWithoutPassingParameter() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val userId = getUserId("user123")
        val account = Account("Fund for something", userId, Currency.BGN, 100f)
        accountRepository.registerAccount(account)
        val accountId = getAccountId("Fund for something", userId)
        val request = HttpPost("http://$domain:$port/v1/executeWithdraw")
        val params = StringEntity("{\"params\":{\"id\":$accountId}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"msg\":\"Cannot execute this withdraw. No account id or " +
                "value passed with the request\"}")))
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

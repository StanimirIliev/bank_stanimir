package com.clouway.app.restservices.post

import com.clouway.app.adapter.http.post.DepositRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.Session
import com.clouway.rules.DataStoreRule
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
import org.junit.Rule
import org.junit.Test
import spark.Spark.*
import java.nio.charset.Charset
import java.time.LocalDateTime

class DepositQueryTest {
    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/deposit/"

    @Before
    fun setUp() {
        port(port)
        post("/v1/deposit/:id", DepositRoute(
                dataStoreRule.sessionRepository,
                dataStoreRule.accountRepository,
                Logger.getLogger(DepositQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun executeDepositAsAuthorizedUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = dataStoreRule.accountRepository.registerAccount(account)
        val request = HttpPost(url + accountId)
        val params = StringEntity("{\"params\":{\"value\":50}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.CREATED_201)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"Deposit successful.\"}")))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(50f)))
    }

    @Test
    fun tryToExecuteDepositAsUnauthorizedUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = dataStoreRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = dataStoreRule.accountRepository.registerAccount(account)
        val request = HttpPost(url + accountId)
        val params = StringEntity("{\"params\":{\"value\":50}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.UNAUTHORIZED_401)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"Cannot execute this deposit. Access denied.\"}")))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(0f)))
    }

    @Test
    fun tryToExecuteDepositWithoutPassingParameter() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = dataStoreRule.accountRepository.registerAccount(account)
        val request = HttpPost(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"Cannot execute this deposit. No account id or " +
                "value passed with the request\"}")))
    }

    private fun createSessionAndCookies(username: String, password: String): CookieStore {
        val userId = dataStoreRule.userRepository.registerUser(username, password)
        val sessionId = dataStoreRule.sessionRepository.registerSession(
                Session(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2)
                )) ?:
                throw Exception("Unable to register the session")
        val cookieStore = BasicCookieStore()
        val cookies = ArrayList<BasicClientCookie>()
        cookies.add(BasicClientCookie("sessionId", sessionId))
        cookies.add(BasicClientCookie("userId", userId.toString()))
        cookies.forEach { it.domain = domain }
        cookies.forEach { it.path = "/" }
        cookieStore.addCookies(cookies.toTypedArray())
        return cookieStore
    }
}

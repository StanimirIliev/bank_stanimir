package com.clouway.app.restservices.post

import com.clouway.app.adapter.http.post.NewAccountRoute
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

class NewAccountQueryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/accounts"

    @Before
    fun setUp() {
        port(port)
        post("/v1/accounts", NewAccountRoute(
                dataStoreRule.sessionRepository,
                dataStoreRule.accountRepository,
                Logger.getLogger(NewAccountQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun addNewAccount() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val request = HttpPost(url)
        val params = StringEntity("{\"params\":{\"title\":\"SomeFund\",\"currency\":\"BGN\"}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.CREATED_201)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"New account opened successful.\"}")))
    }

    @Test
    fun tryToAddTwoAccountsWithTheSameName() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        dataStoreRule.accountRepository.registerAccount(Account("SomeFund", userId, Currency.BGN, 0f))
        val request = HttpPost(url)
        val params = StringEntity("{\"params\":{\"title\":\"SomeFund\",\"currency\":\"BGN\"}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"You have already account with such a title.\"}")))
    }

    @Test
    fun tryToAddAccountWithoutPassingAnParameter() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val request = HttpPost(url)
        val params = StringEntity("{\"params\":{\"title\":\"SomeFund\"}}")
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"Cannot open new account. No title or currency " +
                "passed with the request\"}")))
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

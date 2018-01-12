package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.get.AccountsListRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.Session
import com.clouway.rules.DataStoreRule
import com.google.gson.Gson
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
import org.junit.Rule
import org.junit.Test
import spark.Spark.*
import java.nio.charset.Charset
import java.time.LocalDateTime

class AccountsQueryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/accounts"

    @Before
    fun setUp() {
        port(port)
        get("/v1/accounts", AccountsListRoute(
                dataStoreRule.sessionRepository,
                dataStoreRule.accountRepository,
                Logger.getLogger(AccountDetailsQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getAllAccountsAsRegisteredUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        val firstAccount = Account("Fund for something", userId, Currency.BGN, 0f)
        val secondAccount = Account("Another fund", userId, Currency.BGN, 0f)
        val firstAccountId = dataStoreRule.accountRepository.registerAccount(firstAccount)
        val secondAccountId = dataStoreRule.accountRepository.registerAccount(secondAccount)
        val accountListJson = Gson().toJson(listOf(
                secondAccount.apply { id = secondAccountId },
                firstAccount.apply { id = firstAccountId }
        ))
        val request = HttpGet(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"content\":$accountListJson}")))
    }

    @Test
    fun tryToGetAllAccountsAsUnregisteredUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = dataStoreRule.userRepository.registerUser("otherUser", "password123")
        dataStoreRule.accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        dataStoreRule.accountRepository.registerAccount(Account("Another fund", userId, Currency.BGN, 0f))
        val request = HttpGet(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"content\":[]}")))
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

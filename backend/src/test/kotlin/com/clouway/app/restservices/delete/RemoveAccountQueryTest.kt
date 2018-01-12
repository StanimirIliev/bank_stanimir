package com.clouway.app.restservices.delete

import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.Session
import com.clouway.rules.DataStoreRule
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpDelete
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import spark.Spark.*
import java.nio.charset.Charset
import java.time.LocalDateTime

class RemoveAccountQueryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/accounts/"

    @Before
    fun setUp() {
        port(port)
        delete("/v1/accounts/:id", RemoveAccountRoute(
                dataStoreRule.sessionRepository,
                dataStoreRule.accountRepository,
                Logger.getLogger(RemoveAccountQueryTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun removeAccountAsAuthorizedUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        val accountId = dataStoreRule.accountRepository.registerAccount(Account("Fund for something", userId,
                Currency.BGN, 0f))
        val request = HttpDelete(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"This account has been removed successfully.\"}")))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId), `is`(nullValue()))
    }

    @Test
    fun tryToRemoveAccountAsUnauthorizedUser() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = dataStoreRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = dataStoreRule.accountRepository.registerAccount(account)
        val request = HttpDelete(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo("{\"message\":\"This account does not exist.\"}")))
        assertThat(dataStoreRule.accountRepository.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun tryToRemoveAccountWithoutPassingAParameter() {
        val cookieStore = createSessionAndCookies("user123", "password789")
        val userId = cookieStore.cookies.find { it.name == "userId" }!!.value.toInt()
        dataStoreRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        dataStoreRule.accountRepository.registerAccount(account)
        val request = HttpDelete(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.NOT_FOUND_404)))
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

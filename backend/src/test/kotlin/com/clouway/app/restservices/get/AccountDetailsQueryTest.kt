package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.get.AccountDetailsRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.httpresponse.GetAccountResponseDto
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.rules.RestServicesRule
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
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

class AccountDetailsQueryTest {

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/accounts/"
    private lateinit var cookieStore: CookieStore

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        port(port)
        get("/v1/accounts/:id", Secured(
                restServicesRule.sessionRepository,
                AccountDetailsRoute(restServicesRule.accountRepository),
                restServicesRule.logger
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getAccountDetailsAsAuthorizedUser() {
        val userId = restServicesRule.session.userId
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpGet(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetAccountResponseDto(account.apply { id = accountId })
        ))))
    }

    @Test
    fun tryToGetAccountDetailsAsUnauthorizedUser() {
        val userId = restServicesRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpGet(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.NOT_FOUND_404)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("Account not found.")
        ))))
    }

    @Test
    fun tryToGetAccountDetailsWithIdThatDoesNotExist() {
        val request = HttpGet(url + "-1")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.NOT_FOUND_404)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("Account not found.")
        ))))
    }
}

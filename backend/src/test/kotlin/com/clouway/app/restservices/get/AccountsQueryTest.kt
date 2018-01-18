package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.get.AccountsListRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.httpresponse.HttpResponseAccountsList
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

class AccountsQueryTest {

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/accounts"
    private lateinit var cookieStore: CookieStore

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        port(port)
        get("/v1/accounts", Secured(
                restServicesRule.sessionRepository,
                AccountsListRoute(restServicesRule.accountRepository),
                restServicesRule.logger
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getAllAccountsAsRegisteredUser() {
        val userId = restServicesRule.session.userId
        val firstAccount = Account("Fund for something", userId, Currency.BGN, 0f)
        val secondAccount = Account("Another fund", userId, Currency.BGN, 0f)
        val firstAccountId = restServicesRule.accountRepository.registerAccount(firstAccount)
        val secondAccountId = restServicesRule.accountRepository.registerAccount(secondAccount)
        val request = HttpGet(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpResponseAccountsList(listOf(
                        secondAccount.apply { id = secondAccountId },
                        firstAccount.apply { id = firstAccountId }
                ))
        ))))
    }

    @Test
    fun tryToGetAllAccountsAsUnregisteredUser() {
        val userId = restServicesRule.userRepository.registerUser("otherUser", "password123")
        restServicesRule.accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        restServicesRule.accountRepository.registerAccount(Account("Another fund", userId, Currency.BGN, 0f))
        val request = HttpGet(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpResponseAccountsList(emptyList())))))
    }
}

package com.clouway.bankapp.restservices.post

import com.clouway.bankapp.adapter.http.Secured
import com.clouway.bankapp.adapter.http.post.NewAccountRoute
import com.clouway.bankapp.core.Account
import com.clouway.bankapp.core.Currency
import com.clouway.bankapp.core.httpresponse.HttpResponseMessage
import com.clouway.rules.RestServicesRule
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
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

class NewAccountQueryTest {

    data class Params(val params: TittleAndCurrency)
    data class TittleAndCurrency(val title: String, val currency: Currency?)

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
        post("/v1/accounts", Secured(
                restServicesRule.sessionRepository,
                NewAccountRoute(restServicesRule.accountRepository),
                restServicesRule.logger
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun addNewAccount() {
        val request = HttpPost(url)
        val params = StringEntity(restServicesRule.gson.toJson(Params(TittleAndCurrency("Some fund", Currency.BGN))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.CREATED_201)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpResponseMessage("New account opened successful.")
        ))))
    }

    @Test
    fun tryToAddTwoAccountsWithTheSameName() {
        val userId = restServicesRule.session.userId
        restServicesRule.accountRepository.registerAccount(Account("SomeFund", userId, Currency.BGN, 0f))
        val request = HttpPost(url)
        val params = StringEntity(restServicesRule.gson.toJson(Params(TittleAndCurrency("SomeFund", Currency.BGN))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpResponseMessage("You have already account with such a title.")
        ))))
    }

    @Test
    fun tryToAddAccountWithoutPassingAnParameter() {
        val request = HttpPost(url)
        val params = StringEntity(restServicesRule.gson.toJson(Params(TittleAndCurrency("Some fund", null))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpResponseMessage("Cannot open new account. No title or currency passed with the request.")
        ))))
    }
}

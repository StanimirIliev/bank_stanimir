package com.clouway.app.restservices.post

import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.post.WithdrawRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.httpresponse.GetMessageResponseDto
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

class WithdrawQueryTest {

    data class Params(val params: Value)
    data class Value(val value: Float)

    private val port = 8080
    private val domain = "127.0.0.1"
    private lateinit var cookieStore: CookieStore

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        port(port)
        post("/v1/:id/withdraw", Secured(
                restServicesRule.sessionRepository,
                WithdrawRoute(restServicesRule.accountRepository),
                restServicesRule.logger
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun executeWithdrawAsAuthorizedUser() {
        val userId = restServicesRule.session.userId
        val account = Account("Fund for something", userId, Currency.BGN, 50f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpPost("http://127.0.0.1:8080/v1/$accountId/withdraw")
        val params = StringEntity(restServicesRule.gson.toJson(Params(Value(30f))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.CREATED_201)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("Withdraw successful.")
        ))))
        assertThat(restServicesRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(20f)))
    }

    @Test
    fun tryToExecuteWithdrawBiggerThanBalance() {
        val userId = restServicesRule.session.userId
        val account = Account("Fund for something", userId, Currency.BGN, 20f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpPost("http://127.0.0.1:8080/v1/$accountId/withdraw")
        val params = StringEntity(restServicesRule.gson.toJson(Params(Value(30f))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.BAD_REQUEST_400)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("Cannot execute this withdraw. Not enough balance.")
        ))))
        assertThat(restServicesRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(20f)))
    }

    @Test
    fun tryToExecuteWithdrawAsUnauthorizedUser() {
        val userId = restServicesRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 100f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpPost("http://127.0.0.1:8080/v1/$accountId/withdraw")
        val params = StringEntity(restServicesRule.gson.toJson(Params(Value(50f))))
        request.entity = params
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.UNAUTHORIZED_401)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("Cannot execute this withdraw. Access denied.")
        ))))
        assertThat(restServicesRule.accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(100f)))
    }
}

package com.clouway.app.restservices.delete

import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.core.Account
import com.clouway.app.core.Currency
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.httpresponse.HttpError
import com.clouway.rules.RestServicesRule
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpDelete
import org.apache.http.impl.client.HttpClientBuilder
import org.eclipse.jetty.http.HttpStatus
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import spark.Spark.*
import java.nio.charset.Charset

class RemoveAccountQueryTest {

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
        delete("/v1/accounts/:id", Secured(
                restServicesRule.sessionRepository,
                RemoveAccountRoute(restServicesRule.accountRepository, restServicesRule.logger),
                restServicesRule.logger
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun removeAccountAsAuthorizedUser() {
        val userId = restServicesRule.session.userId
        val accountId = restServicesRule.accountRepository.registerAccount(Account("Fund for something", userId,
                Currency.BGN, 0f))
        val request = HttpDelete(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                GetMessageResponseDto("This account has been removed successfully.")
        ))))
        assertThat(restServicesRule.accountRepository.getUserAccount(userId, accountId), `is`(nullValue()))
    }

    @Test
    fun tryToRemoveAccountAsUnauthorizedUser() {
        val userId = restServicesRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val request = HttpDelete(url + accountId)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.NOT_FOUND_404)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(
                HttpError("Account not found.")
        ))))
        assertThat(restServicesRule.accountRepository.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun tryToRemoveAccountWithoutPassingAParameter() {
        val userId = restServicesRule.session.userId
        restServicesRule.userRepository.registerUser("otherUser", "password123")
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        restServicesRule.accountRepository.registerAccount(account)
        val request = HttpDelete(url)
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.NOT_FOUND_404)))
    }
}

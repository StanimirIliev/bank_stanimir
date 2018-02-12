package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.get.TransactionsRoute
import com.clouway.app.core.*
import com.clouway.app.core.httpresponse.AccountTransactions
import com.clouway.app.core.httpresponse.GetListAccountTransactionsResponseDto
import com.clouway.app.core.httpresponse.GetTransactionsCountResponseDto
import com.clouway.rules.RestServicesRule
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.eclipse.jetty.http.HttpStatus
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import spark.Spark
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.LocalDateTime

class TransactionsQueryTest {

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/transactions/"
    private lateinit var cookieStore: CookieStore

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        Spark.port(port)
        Spark.get("/v1/transactions/:param", Secured(
                restServicesRule.sessionRepository,
                TransactionsRoute(restServicesRule.transactionRepository, restServicesRule.accountRepository),
                restServicesRule.logger
        ), restServicesRule.transformer)
        Spark.awaitInitialization()
    }

    @After
    fun terminate() {
        Spark.stop()
    }

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Test
    fun getTransactionsCount() {
        val userId = restServicesRule.session.userId
        val accountId = restServicesRule.accountRepository.registerAccount(
                Account("Some fund", userId, Currency.BGN, 0f))
        restServicesRule.transactionRepository.registerTransaction(
                Transaction(userId, accountId, LocalDateTime.now(), Operation.DEPOSIT, 50f))
        val request = HttpGet(url + "count")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, CoreMatchers.`is`(CoreMatchers.equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, CoreMatchers.`is`(CoreMatchers.equalTo(restServicesRule.gson.toJson(
                GetTransactionsCountResponseDto(1)
        ))))
    }

    @Test
    fun getTransactionsFromSpecificPage() {
        val userId = restServicesRule.session.userId
        val account = Account("Some fund", userId, Currency.BGN, 0f)
        val accountId = restServicesRule.accountRepository.registerAccount(account)
        val instant = LocalDateTime.of(2018, 1, 18, 18, 17)
        val transaction = Transaction(userId, accountId, instant, Operation.DEPOSIT, 50f)
        val transactionWithTimestamp = TransactionWithTimestamp(userId, accountId, Timestamp.valueOf(instant), Operation.DEPOSIT,
                50f)
        restServicesRule.transactionRepository.registerTransaction(transaction)
        val request = HttpGet(url + "1?pageSize=1")
        val response = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build().execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, CoreMatchers.`is`(CoreMatchers.equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, CoreMatchers.`is`(CoreMatchers.equalTo(restServicesRule.gson.toJson(
                GetListAccountTransactionsResponseDto(listOf(AccountTransactions(account.apply { id = accountId },
                        listOf(transactionWithTimestamp))))
        ))))
    }
}
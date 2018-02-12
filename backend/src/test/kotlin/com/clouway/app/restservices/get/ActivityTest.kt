package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.get.ActivityRoute
import com.clouway.app.core.httpresponse.GetActivityResponseDto
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


class ActivityTest {

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/activity"
    private lateinit var cookieStore: CookieStore

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        port(port)
        get("/v1/activity", ActivityRoute(
                restServicesRule.sessionRepository
        ), restServicesRule.transformer)
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getActiveUsersCountAsRegisteredUserJsonData() {
        val client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
        val request = HttpGet(url)
        val response = client.execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo(restServicesRule.gson.toJson(GetActivityResponseDto(1)))))
    }
}

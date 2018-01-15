package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.get.UsersRoute
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


class UsernameQueryTest {

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/username"
    private lateinit var cookieStore: CookieStore

    @Rule
    @JvmField
    val restServicesRule = RestServicesRule(domain)

    @Before
    fun setUp() {
        cookieStore = restServicesRule.createSessionAndCookie("user123", "password789")
        port(port)
        get("/v1/username", UsersRoute(
                restServicesRule.userRepository,
                restServicesRule.session
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getUsernameAsRegisteredUser() {
        val client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
        val request = HttpGet(url)
        val response = client.execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"username\":\"user123\"}")))
    }
}

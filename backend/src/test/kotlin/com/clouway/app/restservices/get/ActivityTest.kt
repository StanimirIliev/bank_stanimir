package com.clouway.app.restservices.get

import com.clouway.app.adapter.http.get.ActivityRoute
import com.clouway.app.core.Session
import com.clouway.rules.DataStoreRule
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


class ActivityTest {
    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    private val port = 8080
    private val domain = "127.0.0.1"
    private val url = "http://$domain:$port/v1/activity"

    @Before
    fun setUp() {
        port(port)
        get("/v1/activity", ActivityRoute(
                dataStoreRule.sessionRepository,
                Logger.getLogger(ActivityTest::class.java)
        ))
        awaitInitialization()
    }

    @After
    fun terminate() {
        stop()
    }

    @Test
    fun getActiveUsersCountAsRegisteredUserJsonData() {
        val cookieStore = createSessionAndCookie("user123", "password789")
        val client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
        val request = HttpGet(url)
        val response = client.execute(request)
        val responseContent = response.entity.content.readBytes().toString(Charset.defaultCharset())
        assertThat(response.statusLine.statusCode, `is`(equalTo(HttpStatus.OK_200)))
        assertThat(responseContent, `is`(equalTo("{\"activity\":1}")))
    }

    private fun createSessionAndCookie(username: String, password: String): CookieStore {
        val userId = dataStoreRule.userRepository.registerUser(username, password)
        val sessionId = dataStoreRule.sessionRepository.registerSession(
                Session(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2)
                )) ?:
                throw Exception("Unable to register the session")
        val cookieStore = BasicCookieStore()
        val cookie = BasicClientCookie("sessionId", sessionId)
        cookie.domain = domain
        cookie.path = "/"
        cookieStore.addCookie(cookie)
        return cookieStore
    }
}

package com.clouway.app.restservices.delete

import com.clouway.app.App
import com.clouway.app.core.Currency
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.json.JsonHttpContent
import com.google.api.client.json.gson.GsonFactory
import com.google.appengine.api.datastore.*
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class RemoveAccountQueryTest {

    val configDatastore = LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy().setNoStorage(false)
            .setBackingStoreLocation("tmp/local_db.bin")
    val helper = LocalServiceTestHelper(configDatastore)
    lateinit var server: App
    lateinit var datastore: DatastoreService


    @Before
    fun setUp() {
        helper.setUp()
        datastore = DatastoreServiceFactory.getDatastoreService()
        server = App(datastore)
        server.start()// start the configured server
    }

    @After
    fun tearDown() {
        helper.tearDown()
        File(configDatastore.backingStoreLocation).delete()
        server.stop()
    }

    data class Params(val params: Values)
    data class Values(val username: String, val password: String, val confirmPassword: String)
    data class Params1(val title: String, val currency: Currency)
    data class Wrapper(val params: Params1)

    @Test
    fun removeAccountAsAuthorizedUser() {
        //register user
        var response = NetHttpTransport()
                .createRequestFactory()
                .buildPostRequest(
                        GenericUrl("http://127.0.0.1:8080/registration"),
                        JsonHttpContent(GsonFactory.getDefaultInstance(),
                                mapOf(
                                        "username" to "user123",
                                        "password" to "password123",
                                        "confirmPassword" to "password123"
                                ))
                )
                .execute()
        println("Status code: ${response.statusCode}")
        println("Response parse as string: ${response.parseAsString()}")
        assertThat(response.statusCode, `is`(equalTo(200)))
        // create new account
        response = NetHttpTransport()
                .createRequestFactory()
                .buildPostRequest(
                        GenericUrl("http://127.0.0.1:8080/accounts"),
                        JsonHttpContent(GsonFactory.getDefaultInstance(),
                                Wrapper(Params1("Fund for something", Currency.BGN)))
                )
                .execute()
        println("Status code: ${response.statusCode}")
        println("Response parse as string: ${response.parseAsString()}")
        assertThat(response.statusCode, `is`(equalTo(200)))
    }
}

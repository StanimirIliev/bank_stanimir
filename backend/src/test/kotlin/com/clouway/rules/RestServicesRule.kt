package com.clouway.rules

import com.clouway.app.JsonTransformer
import com.clouway.app.core.Session
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.apache.http.client.CookieStore
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.log4j.Logger
import org.junit.rules.ExternalResource
import java.time.LocalDateTime
import java.time.ZoneOffset


class RestServicesRule(private val domain: String) : ExternalResource() {

//    val configDatastore = LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy().setNoStorage(false)
//            .setBackingStoreLocation("tmp/local_db.bin")
    val logger = Logger.getLogger("RestServiceRule")!!
    val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                override fun read(`in`: JsonReader): LocalDateTime {
                    val value = `in`.nextString()
                    return LocalDateTime.ofEpochSecond(value.toLong(), 0, ZoneOffset.UTC)
                }

                override fun write(out: JsonWriter, value: LocalDateTime) {
                    val timestampValue = value.toInstant(ZoneOffset.UTC).epochSecond
                    out.value(timestampValue)
                }
            }).create()!!
    val transformer = JsonTransformer()

    val userId = 1// random value
    val session = Session(userId,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2))

    override fun before() {
    }

    override fun after() {

    }

    fun createCookieStore(sessionId: String): CookieStore {
        val cookieStore = BasicCookieStore()
        val cookie = BasicClientCookie("sessionId", sessionId)
        cookie.domain = domain
        cookie.path = "/"
        cookieStore.addCookie(cookie)
        return cookieStore
    }
}
package com.clouway.app

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import spark.ResponseTransformer
import java.time.LocalDateTime
import java.time.ZoneOffset

class JsonTransformer : ResponseTransformer {

    private val gson = GsonBuilder()
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
            }).create()

    override fun render(model: Any?): String {
        return gson.toJson(model)
    }
}
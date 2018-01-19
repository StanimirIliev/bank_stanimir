package com.clouway.bankapp

import com.google.gson.GsonBuilder
import spark.ResponseTransformer

class JsonTransformer : ResponseTransformer {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun render(model: Any?): String {
        return gson.toJson(model)
    }
}
package com.clouway.app.core

import com.clouway.app.Error

interface RequestValidator {
    fun validate(params: Map<String, Array<String>>): List<Error>
}
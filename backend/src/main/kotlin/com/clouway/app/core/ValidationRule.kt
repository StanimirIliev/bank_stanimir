package com.clouway.app.core

import com.clouway.app.Error

interface ValidationRule {
    fun validate(params: Map<String, Array<String>>): Error?
}
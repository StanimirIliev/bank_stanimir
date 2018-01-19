package com.clouway.bankapp.core

interface RequestValidator {
    fun validate(params: Map<String, Array<String>>): List<Error>
}
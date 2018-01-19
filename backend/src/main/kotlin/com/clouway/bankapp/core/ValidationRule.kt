package com.clouway.bankapp.core

interface ValidationRule {
    fun validate(params: Map<String, Array<String>>): Error?
}
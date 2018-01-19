package com.clouway.bankapp

import com.clouway.bankapp.core.Error
import com.clouway.bankapp.core.RequestValidator
import java.util.*

class CompositeValidator(private vararg var validators: RegexValidationRule) : RequestValidator {
    override fun validate(params: Map<String, Array<String>>): List<Error> {
        return validators.mapNotNullTo(LinkedList()) { it.validate(params) }
    }
}
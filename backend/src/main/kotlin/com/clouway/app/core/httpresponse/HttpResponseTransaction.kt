package com.clouway.app.core.httpresponse

import com.clouway.app.core.Currency
import com.clouway.app.core.Operation
import java.sql.Timestamp

data class HttpResponseTransaction(val userId: Int, val onDate: Timestamp, val operation: Operation,
                                   val title: String, val currency: Currency, val amount: Float)
package com.clouway.bankapp.core.httpresponse

import com.clouway.bankapp.core.Transaction

data class HttpResponseTransactionsList(val transactions: List<Transaction>)
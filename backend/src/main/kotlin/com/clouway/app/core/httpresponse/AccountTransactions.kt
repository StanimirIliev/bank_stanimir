package com.clouway.app.core.httpresponse

import com.clouway.app.core.Account
import com.clouway.app.core.TransactionWithTimestamp

data class AccountTransactions(val account: Account, val transactions: List<TransactionWithTimestamp>)
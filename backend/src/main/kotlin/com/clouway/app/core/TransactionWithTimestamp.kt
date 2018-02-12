package com.clouway.app.core

import com.clouway.app.core.Operation
import java.sql.Timestamp

data class TransactionWithTimestamp(val userId: Int, val accountId: Int, val onDate: Timestamp, val operation: Operation, val amount: Float)
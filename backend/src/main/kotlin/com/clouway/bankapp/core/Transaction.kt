package com.clouway.bankapp.core

import java.time.LocalDateTime


data class Transaction(val userId: Int, val accountId: Int, val onDate: LocalDateTime, val operation: Operation, val amount: Float)
package com.clouway.bankapp.core

import java.time.LocalDateTime

data class Session(val userId: Int, val createdOn: LocalDateTime, val expiresAt: LocalDateTime)
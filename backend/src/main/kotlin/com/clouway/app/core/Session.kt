package com.clouway.app.core

import java.sql.Timestamp

data class Session(val userId: Int, val expiresAt: Timestamp)
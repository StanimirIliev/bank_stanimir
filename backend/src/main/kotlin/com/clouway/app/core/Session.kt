package com.clouway.app.core

import java.sql.Timestamp

class Session(val sessionId: String, val username: String, val expiresAt: Timestamp) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (sessionId != other.sessionId) return false
        if (username != other.username) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + username.hashCode()
        return result
    }
}
package com.clouway.app.core

import java.sql.Timestamp

class Session(val id: String, val userId: Int, val expiresAt: Timestamp) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (id != other.id) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
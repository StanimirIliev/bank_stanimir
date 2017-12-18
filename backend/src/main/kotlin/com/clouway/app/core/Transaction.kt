package com.clouway.app.core

import java.sql.Timestamp

class Transaction(val userId: Int, val onDate: Timestamp, val operation: Operation,
                  val amount: Float) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (userId != other.userId) return false
        if (operation != other.operation) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId
        result = 31 * result + operation.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }
}
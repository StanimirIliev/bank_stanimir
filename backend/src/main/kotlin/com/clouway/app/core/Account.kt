package com.clouway.app.core

class Account(val title: String, val userId: Int, val currency: Currency, val balance: Float, var id: Int = -1) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (title != other.title) return false
        if (userId != other.userId) return false
        if (currency != other.currency) return false
        if (balance != other.balance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + userId
        result = 31 * result + currency.hashCode()
        result = 31 * result + balance.hashCode()
        return result
    }
}
package com.clouway.bankapp.core

data class Account(val title: String, val userId: Int, val currency: Currency, val balance: Float, var id: Int = -1)
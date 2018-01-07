package com.clouway.app.core

import java.sql.Timestamp

interface TransactionRepository {

    /**
     * Registers transaction in the DB
     * @param transaction the DTO which would be registered
     * @return true if the operation was successful, false if problem occurs
     */
    fun registerTransaction(transaction: Transaction): Boolean

    /**
     * Gets list with all transactions made of specific userId
     * @param userId the id of the user which was made these transactions
     * @return sorted list with the transactions of the specific user (ascending order)
     */
    fun getTransactions(userId: Int): List<Transaction>

    /**
     * Gets the id of the transaction by parameters
     * @param userId the id of the user made this transaction
     * @param onDate the timestamp in which the user is made this transaction
     * @return the id of the transaction or null if there is no match with these parameters
     */
    fun getTransactionId(userId: Int, accountId: Int, onDate: Timestamp): Int
}
package com.clouway.app.core

interface AccountRepository {

    /**
     * Registers account linked with userId in the DB
     * @param account the DTO to register
     * @return whether the registration was successful
     */
    fun registerAccount(account: Account): Boolean

    /**
     * Gets the balance of specific account
     * @param accountId the id of the account from which to get balance
     * @return the balance of this account or null if there is account with a such id
     */
    fun getBalance(accountId: Int): Float?

    /**
     * Updates the balance of specific account
     * @param accountId the id of the account which balance to update
     * @param amount the amount to add to the balance of the account
     * For withdraw amount should be less than zero
     * For deposit amount should be greater than zero
     * @return true if the operation was successful, false if the withdraw was greater than the balance
     * or other error
     */
    fun updateBalance(accountId: Int, amount: Float): Boolean

    /**
     * Gets the title of the account by its id
     * @param accountId the id of the account
     * @return the title of the account or null if there is no account with such a id
     */
    fun getTitle(accountId: Int): String?

    /**
     * Gets the account id
     * @param title the title of account
     * @param userId the user id of account
     * @return the id of this account or null if there is no account registered with these parameters
     */
    fun getAccountId(title: String, userId: Int): Int?

    /**
     * Gets user id by its account id
     * @param accountId the id of the user's account
     * @return the id of the user or null if there is no account with such a id
     */
    fun getUserId(accountId: Int): Int?
}
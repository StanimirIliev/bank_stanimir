package com.clouway.app.core

interface AccountRepository {

    /**
     * Registers account linked with userId in the DB
     * @param account the DTO to register
     * @return the id of the account or -1 it the registration was not successful
     */
    fun registerAccount(account: Account): Int

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
     * Gets all accounts by userId
     * @param userId the id of the user
     * @return list with all accounts registered on this userId
     */
    fun getAllAccounts(userId: Int): List<Account>

    /**
     * Removes account from the DB
     * @param userId the id of the user (Used for authorization)
     * @param accountId the id of the account
     * @return true if the account was removed successful, false if was not
     */
    fun removeAccount(accountId: Int): Boolean

    /**
     * Gets user account
     * @param userId the id of the user
     * @param accountId the id of the account
     * @return Account DTO or null if there was not match in the DB
     */
    fun getUserAccount(userId: Int, accountId: Int): Account?
}

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
     * @param userId the id of the user which request this update
     * @param amount the amount to add to the balance of the account
     * For withdraw amount should be less than zero
     * For deposit amount should be greater than zero
     * @return OperationResponse object
     * OperationResponse messages:
     * incorrect-id
     * low-balance
     * invalid-request
     * successful
     * error
     */
    fun updateBalance(accountId: Int, userId: Int, amount: Float): OperationResponse


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
     * @return OperationResponse object
     * OperationResponse messages:
     * account-not-exist
     * successful
     * error
     */
    fun removeAccount(accountId: Int, userId: Int): OperationResponse

    /**
     * Gets user account
     * @param userId the id of the user
     * @param accountId the id of the account
     * @return Account DTO or null if there was not match in the DB
     */
    fun getUserAccount(userId: Int, accountId: Int): Account?
}

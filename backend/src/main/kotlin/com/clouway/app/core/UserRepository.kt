package com.clouway.app.core

interface UserRepository {
    /**
     * Registers users in the DB
     * @param username the username of the new user
     * @param password the password of the new user
     * @return true if the operation was successful, false if there is already registered user with that username
     */
    fun registerUser(username: String, password: String): Boolean

    /**
     * Authenticates the user with specific parameters
     * @param username the username of the user
     * @param password the password of the user
     * @return true if there is match with these parameters in the DB, false if there is not
     */
    fun authenticate(username: String, password: String): Boolean

    /**
     * Gets the id of the user by its username
     * @param username the username of the user
     * @return the id of the user or -1 if there is no match in the DB
     */
    fun getUserId(username: String): Int

    /**
     * Gets the username of the user by username
     * @param id the id of the user
     * @return the username of the user or null if there is no match in the DB
     */
    fun getUsername(id: Int): String?
}

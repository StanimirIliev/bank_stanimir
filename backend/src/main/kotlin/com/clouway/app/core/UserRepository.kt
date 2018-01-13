package com.clouway.app.core

interface UserRepository {
    /**
     * Registers users in the DB
     * @param username the username of the new user
     * @param password the password of the new user
     * @return the id of the user if the operation was successful,
     * -1 if there is already registered user with that username
     */
    fun registerUser(username: String, password: String): Int

    /**
     * Authenticates the user with specific parameters
     * @param username the username of the user
     * @param password the password of the user
     * @return true if there is match with these parameters in the DB, false if there is not
     */
    fun authenticate(username: String, password: String): Boolean
}

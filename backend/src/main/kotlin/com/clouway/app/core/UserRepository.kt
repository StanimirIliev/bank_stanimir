package com.clouway.app.core

interface UserRepository {
    /**
     * Returns null if the user is already registered
     * Returns User object if the user is registered successfully
     */
    fun registerUser(username: String, password: String): User?
    fun authenticate(username: String, password: String): User?
}
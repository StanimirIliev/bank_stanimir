package com.clouway.app.adapter.jdbc

import com.clouway.rules.DataStoreRule
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class JdbcUserRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        dataStoreRule.userRepository.registerUser("user123", "password456")
        assertThat(dataStoreRule.userRepository.registerUser("user123", "password789"),
                `is`(equalTo(-1)))
    }

    @Test
    fun authenticateUserThatWasRegistered() {
        dataStoreRule.userRepository.registerUser("user123", "password789")
        assertThat(dataStoreRule.userRepository.authenticate("user123", "password789"), `is`(equalTo(true)))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        assertThat(dataStoreRule.userRepository.authenticate("user123", "password456"), `is`(equalTo(false)))
    }
}

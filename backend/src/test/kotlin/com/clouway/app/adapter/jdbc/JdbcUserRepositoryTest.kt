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

    @Test
    fun getUsernameOfRegisteredUserByItsId() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password789")
        assertThat(dataStoreRule.userRepository.getUsername(userId), `is`(equalTo("user123")))
    }

    @Test
    fun tryToGetUsernameWithWrongId() {
        assertThat(dataStoreRule.userRepository.getUsername(-1), `is`(nullValue()))
    }

    @Test
    fun getIdOfRegisteredUserByItsUsername() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "password789")
        assertThat(dataStoreRule.userRepository.getUserId("user123"), `is`(equalTo(userId)))
    }

    @Test
    fun tryToGetIdOfUserWithWrongUsername() {
        assertThat(dataStoreRule.userRepository.getUserId("InvalidUsername"), `is`(equalTo(-1)))
    }
}

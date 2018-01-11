package com.clouway.app

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class ValidatorTest {

    private val userCheckExpression = "[a-zA-Z\\d]{4,15}"
    private val passwordCheckExpression = "^[a-zA-Z\\d]{6,30}\$"

    private val usernameError = Error("Incorrect username.\nShould be between 4 and 15 characters long " +
            "and to not contain special symbols.\n")
    private val passwordError = Error("Incorrect password.\nShould be between 6 and 30 characters long, " +
            "should not contain non Latin letters, " +
            "should not contain special symbols.")

    private val compositeValidator = CompositeValidator(RegexValidationRule("username", userCheckExpression,
            usernameError.content),
            RegexValidationRule("password", passwordCheckExpression,
                    passwordError.content))


    private val params = HashMap<String, Array<String>>()

    @Before
    fun setUp() {
        params.clear()
    }

    @Test
    fun happyPath() {
        params.put("username", arrayOf("someone"))
        params.put("password", arrayOf("password123"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf())))
    }

    @Test
    fun correctUsernameContainingMixOfLettersAndDigits() {
        params.put("username", arrayOf("username123"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf())))
    }

    @Test
    fun tooShortUsername() {
        params.put("username", arrayOf("1"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(usernameError))))
    }

    @Test
    fun tooLongUsername() {
        params.put("username", arrayOf("user012345678901253456789"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(usernameError))))
    }

    @Test
    fun usernameWithNonLatinLetters() {
        params.put("username", arrayOf("Потребител"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(usernameError))))
    }

    @Test
    fun usernameWithSymbols() {
        params.put("username", arrayOf("us\$er"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(usernameError))))
    }

    @Test
    fun correctPasswordContainingMixOfLettersAndDigits() {
        params.put("password", arrayOf("password1234"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf())))
    }

    @Test
    fun tooShortPassword() {
        params.put("password", arrayOf("pass"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(passwordError))))
    }

    @Test
    fun tooLongPassword() {
        params.put("password", arrayOf("password012345678901234567890123456789"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(passwordError))))
    }

    @Test
    fun wrongUsernameAndWrongPassword() {
        params.put("username", arrayOf("u"))
        params.put("password", arrayOf("pass"))
        assertThat(compositeValidator.validate(params), `is`(equalTo(listOf(usernameError, passwordError))))
    }
}
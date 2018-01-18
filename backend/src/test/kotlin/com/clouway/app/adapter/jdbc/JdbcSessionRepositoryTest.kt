package com.clouway.app.adapter.jdbc

import com.clouway.app.core.Session
import com.clouway.rules.DataStoreRule
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class JdbcSessionRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()

    @Test
    fun getSessionThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = dataStoreRule.sessionRepository.registerSession(session)
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId!!, instant), `is`(equalTo(session)))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        //TODO
    }

    @Test
    fun tryToBindSessionToUnregisteredUser() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val session = Session(1, createdOn, expiresAt)
        assertThat(dataStoreRule.sessionRepository.registerSession(session), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt("notExistingId", instant), `is`(nullValue()))
    }

    @Test
    fun getSessionsCount() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(dataStoreRule.sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        dataStoreRule.sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(dataStoreRule.sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun getSessionsCountOfOnlyNotExpiredSessions() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(dataStoreRule.sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn1 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt1 = LocalDateTime.of(2018, 1, 12, 15, 10)
        val createdOn2 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt2 = LocalDateTime.of(2018, 1, 12, 14, 15)
        dataStoreRule.sessionRepository.registerSession(Session(userId, createdOn1, expiresAt1))
        dataStoreRule.sessionRepository.registerSession(Session(userId, createdOn2, expiresAt2))
        assertThat(dataStoreRule.sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun terminateSessionThatWasRegistered() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val sessionId = dataStoreRule.sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(dataStoreRule.sessionRepository.terminateSession(sessionId!!), `is`(equalTo(true)))
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateSessionThatWasNotRegistered() {
        assertThat(dataStoreRule.sessionRepository.terminateSession("notExistingId"), `is`(equalTo(false)))
    }

    @Test
    fun terminateInactiveSession() {
        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val activeTime = LocalDateTime.of(2018, 1, 12, 14, 30)
        val instant = LocalDateTime.of(2018, 1, 12, 15, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = dataStoreRule.sessionRepository.registerSession(session)!!
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId, activeTime), `is`(equalTo(session)))
        assertThat(dataStoreRule.sessionRepository.terminateInactiveSessions(instant), `is`(equalTo(1)))
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId, activeTime), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateInactiveSessionsWhenAllSessionsAreActive() {

        val userId = dataStoreRule.userRepository.registerUser("user123", "somePassword")
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 30)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = dataStoreRule.sessionRepository.registerSession(session)!!
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
        assertThat(dataStoreRule.sessionRepository.terminateInactiveSessions(instant), `is`(equalTo(0)))
        assertThat(dataStoreRule.sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
    }
}

package com.clouway.app.core

import java.time.LocalDateTime

interface SessionRepository {

    /**
     * Registers session in the DB
     * @param session the DTO that would be registered
     * @return the session id if the operation was successful, null if it was not
     */
    fun registerSession(session: Session): String?

    /**
     * Gets session from the DB
     * @param sessionId the id of the session
     * @return Session DTO if there is a match with this id or null if there is not
     */
    fun getSession(sessionId: String): Session?

    /**
     * Gets session id from the DB
     * @param userId the id of the user
     * @param createdOn when the session was created
     * @return the session id or null if there is no match in the DB with these params
     */
    fun getSessionId(userId: Int, createdOn: LocalDateTime): String?

    /**
     * Gets the count of all active sessions
     * @return the count of all active sessions
     */
    fun getSessionsCount(): Int

    /**
     * Deletes session from the DB
     * @param sessionId the id of the session which should be deleted
     * @return true if the operation was successful, false if error occurred
     */
    fun terminateSession(sessionId: String): Boolean
}
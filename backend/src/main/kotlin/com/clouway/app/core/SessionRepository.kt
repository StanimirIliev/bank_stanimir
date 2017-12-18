package com.clouway.app.core

import java.sql.Timestamp

interface SessionRepository {

    /**
     * Registers session in the DB
     * @param session the DTO that would be registered
     * @return true if the operation was successful, false if error occurred (like duplicating session's id)
     */
    fun registerSession(session: Session): Boolean

    /**
     * Gets session from the DB
     * @param sessionId the id of the session
     * @return Session DTO if there is a match with this id or null if there is not
     */
    fun getSession(sessionId: String): Session?

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
    fun deleteSession(sessionId: String): Boolean
}
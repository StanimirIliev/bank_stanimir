package com.clouway.app.core

interface SessionRepository {

    /**
     * Returns true if the session was successfully registered in the DB
     * false if this session is already registered
     */
    fun registerSession(session: Session): Boolean

    /**
     * Returns Session object by the session id
     * null if the session was not found by this id or it was expired
     */
    fun getSession(sessionId: String): Session?

    /**
     * Returns the count of all sessions that was not expired yet
     */
    fun getSessionsCount(): Int

    /**
     * Returns true if the operation was successful
     * false if there was not found session with this id
     */
    fun deleteSession(sessionId: String): Boolean
}
package com.clouway.app

import java.util.*

class SessionCounter {

    private val sessions = LinkedList<String>()

    fun sessionDestroyed(sessionId: String) {
        sessions.remove(sessionId)
    }

    fun sessionCreated(sessionId: String) {
        sessions.add(sessionId)
    }

    fun getActiveSessionNumber(): Int {
        return sessions.size
    }
}
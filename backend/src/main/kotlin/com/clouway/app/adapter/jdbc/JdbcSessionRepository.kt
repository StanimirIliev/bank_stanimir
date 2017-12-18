package com.clouway.app.adapter.jdbc

import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

class JdbcSessionRepository(private val jdbcTemplate: JdbcTemplate, private val table: String,
                            private val userTable: String) : SessionRepository {
    override fun registerSession(session: Session): Boolean {
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO $table(Id, UserId, ExpiresAt) " +
                        "VALUES('${session.id}', (SELECT Id FROM $userTable WHERE Id='${session.userId}'),'${session.expiresAt}')")
        return affectedRows == 1
    }

    override fun getSession(sessionId: String): Session? {
        val session = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id='$sessionId'",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(rs.getString("Id"), rs.getInt("UserId"),
                                rs.getTimestamp("ExpiresAt"))
                    }
                })
        return if (session.isEmpty() || session.first().expiresAt.before(Timestamp.valueOf(LocalDateTime.now()))) null
        else session.first()
    }

    override fun getSessionsCount(): Int {
        val sessions = jdbcTemplate.fetch("SELECT * FROM $table",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(rs.getString("Id"), rs.getInt("UserId"),
                                rs.getTimestamp("ExpiresAt"))
                    }
                })
        val activeSessionsCount = sessions.filter { it.expiresAt.after(Timestamp.valueOf(LocalDateTime.now())) }.size
        if (activeSessionsCount != sessions.size) {// Removing expired sessions from the DB
            sessions.filter { it.expiresAt.before(Timestamp.valueOf(LocalDateTime.now())) }
                    .forEach { deleteSession(it.id) }
        }
        return activeSessionsCount
    }

    override fun deleteSession(sessionId: String): Boolean {
        val affectedRows = jdbcTemplate
                .execute("DELETE FROM $table WHERE Id='$sessionId'")
        return affectedRows == 1
    }
}
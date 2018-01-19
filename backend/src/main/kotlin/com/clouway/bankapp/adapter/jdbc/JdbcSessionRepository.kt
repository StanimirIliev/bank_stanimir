package com.clouway.bankapp.adapter.jdbc

import com.clouway.bankapp.core.JdbcTemplate
import com.clouway.bankapp.core.RowMapper
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionRepository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

class JdbcSessionRepository(private val jdbcTemplate: JdbcTemplate) : SessionRepository {
    override fun registerSession(session: Session): String? {
        val id = UUID.randomUUID().toString()
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO Sessions(Id, UserId, CreatedOn, ExpiresAt) " +
                        "VALUES('$id', (SELECT Id FROM Users WHERE Id='${session.userId}'),'${session.createdOn}'," +
                        "'${session.expiresAt}')")
        return if (affectedRows == 1) id else null
    }

    override fun getSessionAvailableAt(sessionId: String, instant: LocalDateTime): Session? {
        val list = jdbcTemplate.fetch("SELECT * FROM Sessions WHERE Id='$sessionId'",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(
                                rs.getInt("UserId"),
                                rs.getTimestamp("CreatedOn").toLocalDateTime(),
                                rs.getTimestamp("ExpiresAt").toLocalDateTime()
                        )
                    }
                })
        return if (!list.isEmpty() && list.first().expiresAt.isAfter(instant))
            list.first()
        else
            null
    }

    override fun getSessionsCount(instant: LocalDateTime): Int {
        val sessions = jdbcTemplate.fetch("SELECT * FROM Sessions",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(
                                rs.getInt("UserId"),
                                rs.getTimestamp("CreatedOn").toLocalDateTime(),
                                rs.getTimestamp("ExpiresAt").toLocalDateTime()
                        )
                    }
                })
        return sessions.filter { it.expiresAt.isAfter(instant) }.count()
    }

    override fun terminateSession(sessionId: String): Boolean {
        val affectedRows = jdbcTemplate
                .execute("DELETE FROM Sessions WHERE Id='$sessionId'")
        return affectedRows == 1
    }

    override fun terminateInactiveSessions(instant: LocalDateTime): Int {
        val sessions = jdbcTemplate.fetch("SELECT * FROM Sessions",
                object : RowMapper<Pair<Session, String>> {
                    override fun fetch(rs: ResultSet): Pair<Session, String> {
                        return Pair(Session(
                                rs.getInt("UserId"),
                                rs.getTimestamp("CreatedOn").toLocalDateTime(),
                                rs.getTimestamp("ExpiresAt").toLocalDateTime()
                        ), rs.getString("Id"))
                    }
                })
        val inactiveSessions = sessions.filter { it.first.expiresAt.isBefore(instant) }
        inactiveSessions.forEach {terminateSession(it.second)}
        return inactiveSessions.count()
    }
}
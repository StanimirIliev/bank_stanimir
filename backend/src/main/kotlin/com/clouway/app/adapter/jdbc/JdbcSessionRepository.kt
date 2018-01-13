package com.clouway.app.adapter.jdbc

import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

class JdbcSessionRepository(
        private val jdbcTemplate: JdbcTemplate,
        private val table: String,
        private val userTable: String
) : SessionRepository {
    override fun registerSession(session: Session): String? {
        val id = UUID.randomUUID().toString()
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO $table(Id, UserId, CreatedOn, ExpiresAt) " +
                        "VALUES('$id', (SELECT Id FROM $userTable WHERE Id='${session.userId}'),'${session.createdOn}'," +
                        "'${session.expiresAt}')")
        return if (affectedRows == 1) id else null
    }

    override fun getSessionAvailableAt(sessionId: String, instant: LocalDateTime): Session? {
        val list = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id='$sessionId'",
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
        val sessions = jdbcTemplate.fetch("SELECT * FROM $table",
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
                .execute("DELETE FROM $table WHERE Id='$sessionId'")
        return affectedRows == 1
    }
}
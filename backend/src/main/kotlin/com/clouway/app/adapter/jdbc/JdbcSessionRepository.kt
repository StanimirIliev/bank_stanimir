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
                .execute("INSERT INTO $table(Id, UserId, CreatedOn) " +
                        "VALUES('$id', (SELECT Id FROM $userTable WHERE Id='${session.userId}'),'${session.createdOn}')")
        return if (affectedRows == 1) id else null
    }

    override fun getSession(sessionId: String): Session? {
        val list = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id='$sessionId'",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(rs.getInt("UserId"), rs.getTimestamp("CreatedOn").toLocalDateTime())
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun getSessionsCount(): Int {
        return jdbcTemplate.fetch("SELECT * FROM $table",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(
                                rs.getInt("UserId"),
                                rs.getTimestamp("CreatedOn").toLocalDateTime()
                        )
                    }
                }).count()
    }

    override fun getSessionId(userId: Int, createdOn: LocalDateTime): String? {
        val list = jdbcTemplate.fetch("SELECT Id FROM $table WHERE UserId=$userId AND CreatedOn='$createdOn'",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Id")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun terminateSession(sessionId: String): Boolean {
        val affectedRows = jdbcTemplate
                .execute("DELETE FROM $table WHERE Id='$sessionId'")
        return affectedRows == 1
    }
}
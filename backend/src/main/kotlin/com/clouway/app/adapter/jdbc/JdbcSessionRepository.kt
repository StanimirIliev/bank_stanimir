package com.clouway.app.adapter.jdbc

import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

class JdbcSessionRepository(private val jdbcTemplate: JdbcTemplate, private val table: String,
                            private val userTable: String) : SessionRepository {
    override fun registerSession(session: Session): String? {
        val availableIds = jdbcTemplate.fetch("SELECT Id FROM $table",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Id")
                    }
                })
        var newId = getRandomString(20)
        while (availableIds.contains(newId)) {// Get unique Id
            newId = getRandomString(20)
        }
        session.expiresAt.nanos = 0//   mysql rounding issue
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO $table(Id, UserId, ExpiresAt) " +
                        "VALUES('$newId', (SELECT Id FROM $userTable WHERE Id='${session.userId}'),'${session.expiresAt}')")
        return if (affectedRows == 1) newId else null
    }

    override fun getSession(sessionId: String): Session? {
        val session = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id='$sessionId'",
                object : RowMapper<Session> {
                    override fun fetch(rs: ResultSet): Session {
                        return Session(rs.getInt("UserId"),
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
                        return Session(rs.getInt("UserId"),
                                rs.getTimestamp("ExpiresAt"))
                    }
                })
        val activeSessionsCount = sessions.filter { it.expiresAt.after(Timestamp.valueOf(LocalDateTime.now())) }.size
        if (activeSessionsCount != sessions.size) {// Removing expired sessions from the DB
            sessions.filter { it.expiresAt.before(Timestamp.valueOf(LocalDateTime.now())) }
                    .forEach { deleteSession(getSessionId(it.userId, it.expiresAt) ?: "") }
        }
        return activeSessionsCount
    }

    override fun getSessionId(userId: Int, expiresAt: Timestamp): String? {
        val list = jdbcTemplate.fetch("SELECT Id FROM $table WHERE UserId=$userId AND ExpiresAt='$expiresAt'",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Id")
                    }
                })
        return if(list.isEmpty()) null else list.first()
    }

    override fun deleteSession(sessionId: String): Boolean {
        val affectedRows = jdbcTemplate
                .execute("DELETE FROM $table WHERE Id='$sessionId'")
        return affectedRows == 1
    }

    private fun getRandomString(size: Int): String {
        val random = Random()
        val numbers = IntArray(size)
        val result = StringBuilder()
        for (i in numbers.indices) {// 48-57 digits 65-90 uppercase letters 97-122 lowercase letters
            numbers[i] = random.nextInt(74) + 48
            while (numbers[i] in 58..64 || numbers[i] in 91..96) {
                numbers[i] = random.nextInt(74) + 48
            }
        }
        numbers.forEach { result.append(it.toChar()) }
        return result.toString()
    }
}
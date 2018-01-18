package com.clouway.app.adapter.jdbc

import com.clouway.app.SaltedHash
import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import com.clouway.app.core.UserRepository
import org.apache.commons.codec.digest.DigestUtils
import java.sql.ResultSet

class JdbcUserRepository(private val jdbcTemplate: JdbcTemplate) : UserRepository {

    override fun registerUser(username: String, password: String): Int {
        val saltedHash = SaltedHash(30, password)
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO Users(Username, Password, Salt) VALUES('$username', '${saltedHash.hash}', " +
                        "'${saltedHash.salt}')")
        if (affectedRows == 1) {
            val list = jdbcTemplate.fetch("SELECT Id FROM Users WHERE Username='$username'",
                    object : RowMapper<Int> {
                        override fun fetch(rs: ResultSet): Int {
                            return rs.getInt("Id")
                        }
                    })
            return if (list.isEmpty()) -1 else list.first()
        }
        return -1
    }

    override fun authenticate(username: String, password: String): Boolean {
        val salt = jdbcTemplate.fetch("SELECT Salt FROM Users WHERE Username='$username'",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Salt")
                    }
                })
        if (salt.isEmpty()) {
            return false
        }
        val list = jdbcTemplate
                .fetch("SELECT * FROM Users WHERE Username='$username' " +
                        "AND Password='${DigestUtils.sha256Hex(salt.first() + password)}'",
                        object : RowMapper<Boolean> {
                            override fun fetch(rs: ResultSet): Boolean {
                                return rs.getString("Username") != null
                            }
                        })
        return if (list.isEmpty()) false else list.first()
    }

    override fun getUsername(id: Int): String? {
        val list = jdbcTemplate.fetch("SELECT Username FROM Users WHERE Id=$id",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Username")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun getUserId(username: String): Int {
        val list = jdbcTemplate.fetch("SELECT Id FROM Users WHERE Username='$username'",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("Id")
                    }
                })
        return if (list.isEmpty()) -1 else list.first()
    }
}
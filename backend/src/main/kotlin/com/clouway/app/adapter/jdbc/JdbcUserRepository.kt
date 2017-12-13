package com.clouway.app.adapter.jdbc

import com.clouway.app.SaltedHash
import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import com.clouway.app.core.User
import com.clouway.app.core.UserRepository
import org.apache.commons.codec.digest.DigestUtils
import java.sql.ResultSet

class JdbcUserRepository(private val jdbcTemplate: JdbcTemplate, private val table: String) : UserRepository {
    /**
     * Returns false if the user is already registered
     * Returns true if the user is registered successfully
     */
    override fun registerUser(username: String, password: String): User? {
        val saltedHash = SaltedHash(30, password)
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO $table(Username, Password, Salt) VALUES('$username', '${saltedHash.hash}', " +
                        "'${saltedHash.salt}')")
        return if (affectedRows != 1) null else authenticate(username, password)
    }

    override fun authenticate(username: String, password: String): User? {
        val salt = jdbcTemplate.fetch("SELECT Salt FROM $table WHERE Username='$username'",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Salt")
                    }
                })
        if (salt.isEmpty()) {
            return null
        }
        val list = jdbcTemplate
                .fetch("SELECT * FROM $table WHERE Username='$username' " +
                        "AND Password='${DigestUtils.sha256Hex(salt.first() + password)}'",
                        object : RowMapper<User> {
                            override fun fetch(rs: ResultSet): User {
                                return User(rs.getInt("Id"), rs.getString("Username"), rs.getString("Password"))
                            }
                        })
        return if (list.isEmpty()) null else list.first()
    }

    override fun getBalance(username: String): Float? {
        val balance = jdbcTemplate.fetch("SELECT Balance FROM $table WHERE Username='$username'",
                object : RowMapper<Float?> {
                    override fun fetch(rs: ResultSet): Float? {
                        return rs.getFloat("Balance")
                    }
                })
        return if (balance.isEmpty()) null else balance.first()
    }
}
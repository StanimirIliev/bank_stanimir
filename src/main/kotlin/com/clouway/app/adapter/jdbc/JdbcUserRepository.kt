package com.clouway.app.adapter.jdbc

import com.clouway.app.SaltedHash
import com.clouway.app.UserRowMapper
import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.User
import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.UserRepository
import org.apache.commons.codec.digest.DigestUtils

class JdbcUserRepository(private val jdbcTemplate: JdbcTemplate, private val table: String) : UserRepository {
    /**
     * Returns false if the user is already registered
     * Returns true if the user is registered successfully
     */
    override fun registerUser(username: String, password: String): User? {
        val saltedHash = SaltedHash(30, password)
        val affectedRows = jdbcTemplate
                .execute("INSERT INTO Users(Username, Password, Salt) VALUES('$username', '${saltedHash.hash}', " +
                        "'${saltedHash.salt}')")
        return if (affectedRows != 1) null else authenticate(username, password)
    }

    override fun authenticate(username: String, password: String): User? {
        val salt = jdbcTemplate.getString("SELECT Salt FROM $table WHERE Username='$username'", "Salt")
                ?: return null
        val list = jdbcTemplate
                .fetch("SELECT * FROM $table WHERE Username='$username' " +
                        "AND Password='${DigestUtils.sha256Hex(salt + password)}'",
                        UserRowMapper())
        return if(list.isEmpty()) null else list.first()
    }
}

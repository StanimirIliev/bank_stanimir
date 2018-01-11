package com.clouway.app

import com.clouway.app.core.RowMapper
import com.clouway.app.core.User
import java.sql.ResultSet

class UserRowMapper: RowMapper<User> {
    override fun fetch(rs: ResultSet): User {
        return User(rs.getInt("Id"), rs.getString("Username"), rs.getString("Password"))
    }
}
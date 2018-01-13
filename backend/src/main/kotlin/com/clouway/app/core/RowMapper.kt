package com.clouway.app.core

import java.sql.ResultSet

interface RowMapper<T> {
    fun fetch(rs: ResultSet): T
}
package com.clouway.app.core

interface JdbcTemplate {
    fun execute(query: String): Int
    fun execute(query: String, vararg params: Any): Int
    fun <T> fetch(query: String, mapper: RowMapper<T>): List<T>
}

package com.clouway.app

import com.clouway.app.core.JdbcTemplate
import com.clouway.app.core.RowMapper
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import javax.sql.DataSource

class MySQLJdbcTemplate(private val dataSource: DataSource) : JdbcTemplate {
    override fun execute(query: String): Int {
        val connection = dataSource.connection
        var statement: Statement? = null
        try {
            statement = connection.prepareStatement(query)
            statement.execute()
            return statement.updateCount
        } catch (e: SQLException) {
            return -1
        } finally {
            if (statement != null) {
                statement.close()
            }
            connection.close()
        }
    }

    override fun execute(query: String, vararg params: Any): Int {
        val connection = dataSource.connection
        var preparedStatement: PreparedStatement? = null
        try {
            preparedStatement = connection.prepareStatement(query)
            for (i in params.indices) {
                preparedStatement.setObject(i + 1, params[i])
            }
            preparedStatement = connection.prepareStatement(query)
            preparedStatement.execute()
            return preparedStatement.updateCount
        } catch (e: SQLException) {
            return -1
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close()
            }
            connection.close()
        }
    }

    override fun <T> fetch(query: String, mapper: RowMapper<T>): List<T> {
        val connection = dataSource.connection
        var statement: Statement? = null
        try {
            statement = connection.prepareStatement(query)
            val result = statement.executeQuery()
            if (!result.next()) {
                return listOf()
            }
            val list = LinkedList<T>()
            do {
                list.add(mapper.fetch(result))
            } while (result.next())
            return list
        } finally {
            if (statement != null) {
                statement.close()
            }
            connection.close()
        }
    }
}

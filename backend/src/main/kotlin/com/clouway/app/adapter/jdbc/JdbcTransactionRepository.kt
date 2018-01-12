package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import java.sql.ResultSet

class JdbcTransactionRepository(
        private val jdbcTemplate: JdbcTemplate,
        private val table: String
) : TransactionRepository {
    override fun registerTransaction(transaction: Transaction): Boolean {
        return jdbcTemplate.execute("INSERT INTO $table(UserId, AccountId, OnDate, Operation, Amount) " +
                "VALUES(${transaction.userId}, ${transaction.accountId}, '${transaction.onDate}'," +
                "'${transaction.operation}', ${transaction.amount})") == 1
    }

    override fun getTransactions(userId: Int): List<Transaction> {
        return jdbcTemplate.fetch("SELECT * FROM $table WHERE UserId='$userId' ORDER BY OnDate",
                object : RowMapper<Transaction> {
                    override fun fetch(rs: ResultSet): Transaction {
                        return Transaction(rs.getInt("UserId"), rs.getInt("AccountId"),
                                rs.getTimestamp("OnDate").toLocalDateTime(), Operation.valueOf(rs.getString("Operation")),
                                rs.getFloat("Amount"))
                    }
                })
    }
}

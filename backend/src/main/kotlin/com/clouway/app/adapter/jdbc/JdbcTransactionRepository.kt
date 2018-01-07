package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import java.sql.ResultSet
import java.sql.Timestamp

class JdbcTransactionRepository(private val jdbcTemplate: JdbcTemplate, private val table: String) :
        TransactionRepository {
    override fun registerTransaction(transaction: Transaction): Boolean {
        //  nanos are set to null due to mysql rounding issue
        return jdbcTemplate.execute("INSERT INTO $table(UserId, AccountId, OnDate, Operation, Amount) " +
                "VALUES(${transaction.userId}, ${transaction.accountId}, '${transaction.onDate.apply { nanos = 0 }}'," +
                "'${transaction.operation}', ${transaction.amount})") == 1
    }

    override fun getTransactions(userId: Int): List<Transaction> {
        return jdbcTemplate.fetch("SELECT * FROM $table WHERE UserId='$userId' ORDER BY OnDate",
                object : RowMapper<Transaction> {
                    override fun fetch(rs: ResultSet): Transaction {
                        return Transaction(rs.getInt("UserId"), rs.getInt("AccountId"),
                                rs.getTimestamp("OnDate"), Operation.valueOf(rs.getString("Operation")),
                                rs.getFloat("Amount"))
                    }
                })
    }

    override fun getTransactionId(userId: Int, accountId: Int, onDate: Timestamp): Int {
        val list = jdbcTemplate.fetch("SELECT Id FROM $table WHERE UserId=$userId AND AccountId=$accountId AND " +
                "OnDate='$onDate'",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("Id")
                    }
                })
        return if (list.isEmpty()) -1 else list.first()
    }
}

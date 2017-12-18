package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

class JdbcAccountRepository(private val jdbcTemplate: JdbcTemplate,
                            private val transactionRepository: TransactionRepository,
                            private val table: String) : AccountRepository {
    override fun registerAccount(account: Account): Boolean {
        return jdbcTemplate.execute("INSERT INTO $table(Title, UserId, Currency, Balance) " +
                "VALUES('${account.title}', ${account.userId}, '${account.currency}', " +
                "${account.balance})") == 1
    }

    override fun getBalance(accountId: Int): Float? {
        val list = jdbcTemplate.fetch("SELECT Balance FROM $table WHERE Id=$accountId",
                object : RowMapper<Float> {
                    override fun fetch(rs: ResultSet): Float {
                        return rs.getFloat("Balance")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun updateBalance(accountId: Int, amount: Float): Boolean {
        val balance = getBalance(accountId) ?: return false
        if (amount + balance < 0) {
            return false
        }
        if (amount == 0f) {
            return true//   No need to update
        }
        if (jdbcTemplate.execute("UPDATE $table SET Balance=${amount + balance} WHERE Id=$accountId") == 1) {
            val userId = getUserId(accountId) ?: return false
            return transactionRepository.registerTransaction(Transaction(userId,
                    Timestamp.valueOf(LocalDateTime.now()), if (amount < 0) Operation.WITHDRAW else Operation.DEPOSIT,
                    amount))
        }
        return false
    }

    override fun getTitle(accountId: Int): String? {
        val list = jdbcTemplate.fetch("SELECT Title FROM $table WHERE Id=$accountId",
                object : RowMapper<String> {
                    override fun fetch(rs: ResultSet): String {
                        return rs.getString("Title")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun getAccountId(title: String, userId: Int): Int? {
        val list = jdbcTemplate.fetch("SELECT Id FROM $table WHERE Title='$title' AND UserId=$userId",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("Id")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    override fun getUserId(accountId: Int): Int? {
        val list = jdbcTemplate.fetch("SELECT UserId FROM $table WHERE Id=$accountId",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("UserId")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }
}
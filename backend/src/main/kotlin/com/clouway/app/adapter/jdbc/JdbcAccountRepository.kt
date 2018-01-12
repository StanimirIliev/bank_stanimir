package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import java.sql.ResultSet
import java.time.LocalDateTime

class JdbcAccountRepository(
        private val jdbcTemplate: JdbcTemplate,
        private val transactionRepository: TransactionRepository,
        private val table: String
) : AccountRepository {
    override fun registerAccount(account: Account): Int {
        val affectedRows = jdbcTemplate.execute("INSERT INTO $table(Title, UserId, Currency, Balance) " +
                "VALUES('${account.title}', ${account.userId}, '${account.currency}', " +
                "${account.balance})")
        val list = jdbcTemplate.fetch("SELECT Id FROM $table WHERE Title='${account.title}' " +
                "AND UserId=${account.userId}",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("Id")
                    }
                })
        return if (affectedRows != 1 || list.isEmpty()) -1 else list.first()
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
            return transactionRepository.registerTransaction(Transaction(userId, accountId,
                    LocalDateTime.now(), if (amount < 0) Operation.WITHDRAW else Operation.DEPOSIT,
                    amount))
        }
        return false
    }

    override fun getAllAccounts(userId: Int): List<Account> {
        return jdbcTemplate.fetch("SELECT * FROM $table WHERE UserId=$userId",
                object : RowMapper<Account> {
                    override fun fetch(rs: ResultSet): Account {
                        return Account(rs.getString("Title"), userId,
                                Currency.valueOf(rs.getString("Currency")), rs.getFloat("Balance"),
                                rs.getInt("Id"))
                    }
                })
    }

    override fun removeAccount(accountId: Int): Boolean {
        return jdbcTemplate.execute("DELETE FROM $table WHERE Id=$accountId") == 1
    }

    override fun getUserAccount(userId: Int, accountId: Int): Account? {
        val list = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id=$accountId AND UserId=$userId",
                object : RowMapper<Account> {
                    override fun fetch(rs: ResultSet): Account {
                        return Account(rs.getString("Title"), rs.getInt("UserId"),
                                Currency.valueOf(rs.getString("Currency")), rs.getFloat("Balance"), accountId)
                    }
                })
        return if(list.isEmpty()) null else list.first()
    }

    private fun getBalance(accountId: Int): Float? {
        val list = jdbcTemplate.fetch("SELECT Balance FROM $table WHERE Id=$accountId",
                object : RowMapper<Float> {
                    override fun fetch(rs: ResultSet): Float {
                        return rs.getFloat("Balance")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }

    private fun getUserId(accountId: Int): Int? {
        val list = jdbcTemplate.fetch("SELECT UserId FROM $table WHERE Id=$accountId",
                object : RowMapper<Int> {
                    override fun fetch(rs: ResultSet): Int {
                        return rs.getInt("UserId")
                    }
                })
        return if (list.isEmpty()) null else list.first()
    }
}

package com.clouway.app.adapter.jdbc

import com.clouway.app.core.*
import java.sql.ResultSet
import java.time.LocalDateTime
import com.clouway.app.core.ErrorType.*
import com.clouway.app.core.Operation.*

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

    override fun updateBalance(accountId: Int, userId: Int, amount: Float): OperationResponse {
        val balance = getBalance(accountId) ?:
                return OperationResponse(false, INCORRECT_ID)
        if (amount + balance < 0) {
            return OperationResponse(false, LOW_BALANCE)
        }
        if (amount == 0f) {
            return OperationResponse(false, INVALID_REQUEST)
        }
        if (jdbcTemplate.execute("UPDATE $table SET Balance=${amount + balance} WHERE Id=$accountId " +
                "AND UserId=$userId") == 1 &&
                transactionRepository.registerTransaction(Transaction(userId, accountId, LocalDateTime.now(),
                        if (amount < 0) WITHDRAW else DEPOSIT, amount))) {
            return OperationResponse(true, null)
        }
        return OperationResponse(false, ACCESS_DENIED)
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

    override fun removeAccount(accountId: Int, userId: Int): OperationResponse {
        if (getUserAccount(userId, accountId) == null) {
            return OperationResponse(false, ACCOUNT_NOT_FOUND)
        }
        if (jdbcTemplate.execute("DELETE FROM $table WHERE Id=$accountId") == 1) {
            return OperationResponse(true, null)
        }
        return OperationResponse(false, INTERNAL_ERROR)
    }

    override fun getUserAccount(userId: Int, accountId: Int): Account? {
        val list = jdbcTemplate.fetch("SELECT * FROM $table WHERE Id=$accountId AND UserId=$userId",
                object : RowMapper<Account> {
                    override fun fetch(rs: ResultSet): Account {
                        return Account(rs.getString("Title"), rs.getInt("UserId"),
                                Currency.valueOf(rs.getString("Currency")), rs.getFloat("Balance"), accountId)
                    }
                })
        return if (list.isEmpty()) null else list.first()
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
}

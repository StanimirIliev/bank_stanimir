package com.clouway.rules

import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.*
import com.mysql.cj.jdbc.MysqlDataSource
import org.junit.rules.ExternalResource
import java.io.FileReader

class DataStoreRule() : ExternalResource() {

    val mySqlDataSource = MysqlDataSource()
    lateinit var accountRepository: AccountRepository
    lateinit var transactionRepository: TransactionRepository
    lateinit var userRepository: UserRepository
    lateinit var sessionRepository: SessionRepository
    lateinit var mySqlJdbcTemplate: JdbcTemplate

    override fun before() {
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}" +
                "?allowMultiQueries=true")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))
        mySqlJdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
        transactionRepository = JdbcTransactionRepository(mySqlJdbcTemplate)
        accountRepository = JdbcAccountRepository(mySqlJdbcTemplate, transactionRepository)
        userRepository = JdbcUserRepository(mySqlJdbcTemplate)
        sessionRepository = JdbcSessionRepository(mySqlJdbcTemplate)
        val statement = mySqlDataSource.connection.createStatement()
        statement.execute(FileReader("schema/create_tables.sql").readText())
        statement.execute(FileReader("schema/clear_tables.sql").readText())
    }
}
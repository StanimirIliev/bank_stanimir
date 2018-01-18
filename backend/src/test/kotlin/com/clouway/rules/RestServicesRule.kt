package com.clouway.rules

import com.clouway.app.JsonTransformer
import com.clouway.app.MySQLJdbcTemplate
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mysql.cj.jdbc.MysqlDataSource
import org.apache.http.client.CookieStore
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.log4j.Logger
import org.junit.rules.ExternalResource
import java.io.FileReader
import java.time.LocalDateTime

class RestServicesRule(private val domain: String) : ExternalResource() {

    private val mySqlDataSource = MysqlDataSource()
    lateinit var accountRepository: AccountRepository
    lateinit var transactionRepository: TransactionRepository
    lateinit var userRepository: UserRepository
    lateinit var sessionRepository: SessionRepository
    lateinit var mySqlJdbcTemplate: JdbcTemplate
    lateinit var session: Session
    val logger = Logger.getLogger("RestServiceRule")!!
    val gson = GsonBuilder().setPrettyPrinting().create()!!
    val transformer = JsonTransformer()

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

    fun createSessionAndCookie(username: String, password: String): CookieStore {
        val userId = userRepository.registerUser(username, password)
        session = Session(
                userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        )
        val sessionId = sessionRepository.registerSession(session) ?:
                throw Exception("Unable to register the session")
        val cookieStore = BasicCookieStore()
        val cookie = BasicClientCookie("sessionId", sessionId)
        cookie.domain = domain
        cookie.path = "/"
        cookieStore.addCookie(cookie)
        return cookieStore
    }
}
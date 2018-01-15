package com.clouway.app

import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.adapter.http.get.*
import com.clouway.app.adapter.http.post.*
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.clouway.app.core.Session
import com.mysql.cj.jdbc.MysqlDataSource
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark.*
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime


fun main(args: Array<String>) {
    val sessionsTable = "Sessions"
    val usersTable = "Users"
    val transactionsTable = "Transactions"
    val accountsTable = "Accounts"

    val config = Configuration(Configuration.VERSION_2_3_23)
    config.setDirectoryForTemplateLoading(File("freemarker/templates"))
    config.defaultEncoding = "UTF-8"
    config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    config.logTemplateExceptions = false

    val mySqlDataSource = MysqlDataSource()
    mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}" +
            "?allowMultiQueries=true")
    mySqlDataSource.user = System.getenv("DB_USER")
    mySqlDataSource.setPassword(System.getenv("DB_PASS"))
    val jdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)
    jdbcTemplate.execute(FileReader("schema/create_tables.sql").readText())// Creates tables if they are missing

    val sessionRepository = JdbcSessionRepository(jdbcTemplate, sessionsTable, usersTable)
    val transactionRepository = JdbcTransactionRepository(jdbcTemplate, transactionsTable)
    val accountRepository = JdbcAccountRepository(jdbcTemplate, transactionRepository, accountsTable)
    val userRepository = JdbcUserRepository(jdbcTemplate, usersTable)
    val compositeValidator = CompositeValidator(
            RegexValidationRule(
                    "userId",
                    "[a-zA-Z\\d]{4,15}",
                    "Incorrect userId.\nShould be between 4 and 15 characters long " +
                            "and to not contain special symbols.\n"
            ),
            RegexValidationRule("password",
                    "^[a-zA-Z\\d]{6,30}\$",
                    "Incorrect password.\nShould be between 6 and 30 characters long, " +
                            "and to not contain special symbols."
            ))

    val logger = Logger.getLogger("App")
    var session = Session(-1, LocalDateTime.MIN, LocalDateTime.MAX)
    val accountsListRoute = AccountsListRoute(accountRepository, session)
    val accountDetailsRoute = AccountDetailsRoute(accountRepository, session)
    val newAccountRoute = NewAccountRoute(accountRepository, session)
    val depositRoute = DepositRoute(accountRepository, session)
    val withdrawRoute = WithdrawRoute(accountRepository, session)
    val removeAccountRoute = RemoveAccountRoute(accountRepository, session, logger)
    val usersRoute = UsersRoute(userRepository, session)

    initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
    internalServerError { _, res ->
        res.type("image/jpeg")
        val image = UsersRoute::class.java.getResourceAsStream("/images/500-wallpaper.jpg")
        image.copyTo(res.raw().outputStream)
    }

    port(8080)

    get("/assets/*", ResourcesRoute())
    get("/static/*", StaticFilesRoute())
    get("/index", IndexPageRoute())
    post("/login", LoginPageRoutePost(userRepository, sessionRepository, config))
    get("/login", LoginPageRouteGet(config))
    get("/registration", RegistrationPageRouteGet(config))
    post("/registration", RegistrationPageRoutePost(userRepository, sessionRepository, compositeValidator, config))
    get("/home", HomePageRoute())
    get("/logout", LogoutRoute(sessionRepository, logger))
    path("/v1") {
        before("/*") { req, resp ->
            try {
                session = sessionRepository.getSessionAvailableAt(req.cookie("sessionId")!!, LocalDateTime.now())!!
                accountsListRoute.session = session
                accountDetailsRoute.session = session
                newAccountRoute.session = session
                depositRoute.session = session
                withdrawRoute.session = session
                removeAccountRoute.session = session
                usersRoute.session = session
            } catch (e: NullPointerException) {
                logger.error("Invalid session")
                halt("{\"message\":\"Invalid session.\"}")
            } finally {
                resp.type("application/json")
            }
        }
        path("/accounts") {
            get("", accountsListRoute)
            get("/:id", accountDetailsRoute)
            post("", newAccountRoute)
            post("/:id/deposit", depositRoute)
            post("/:id/withdraw", withdrawRoute)
            delete("/:id", removeAccountRoute)
        }
        get("/activity", ActivityRoute(sessionRepository))
        get("/username", usersRoute)
    }
    get("/*") { _, res -> res.redirect("/home") }

}

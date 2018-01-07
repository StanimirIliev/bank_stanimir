package com.clouway.app

import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.adapter.http.get.*
import com.clouway.app.adapter.http.post.*
import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.mysql.cj.jdbc.MysqlDataSource
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark.*
import java.io.File


class App

fun main(args: Array<String>) {
    val sessionsTable = "Sessions"
    val usersTable = "Users"
    val transactionsTable = "Transactions"
    val accountsTable = "Accounts"

    val config = Configuration(Configuration.VERSION_2_3_23)
    config.setDirectoryForTemplateLoading(File("backend/freemarker/templates"))//backend/freemarker/templates
    config.defaultEncoding = "UTF-8"
    config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    config.logTemplateExceptions = false

    val mySqlDataSource = MysqlDataSource()
    mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
    mySqlDataSource.user = System.getenv("DB_USER")
    mySqlDataSource.setPassword(System.getenv("DB_PASS"))
    val jdbcTemplate = MySQLJdbcTemplate(mySqlDataSource)

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

    val logger = Logger.getLogger(App::class.java)

    initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
    internalServerError { _, res ->
        res.type("image/jpeg")
        val image = App::class.java.getResourceAsStream("/images/500-wallpaper.jpg")
        image.copyTo(res.raw().outputStream)
    }

    port(8080)

    get("/assets/*", ResourcesRoute())
    get("/static/*", StaticFilesRoute())
    get("/index", IndexPageRoute())
    post("/login", LoginPageRoutePost(userRepository, sessionRepository, config))
    get("/login", LoginPageRouteGet(config))
    get("/registration", RegistrationPageRouteGet(config))
    post("/registration", RegistrationPageRoutePOST(userRepository, sessionRepository, compositeValidator, config))
    get("/home", HomePageRoute())
    get("/logout", LogoutRoute(sessionRepository, logger))
    path("/v1") {
        get("/account", AccountDetailsRoute(sessionRepository, accountRepository, logger))
        get("/accounts", AccountsListRoute(sessionRepository, accountRepository, logger))
        get("/activeUsers", ActiveUsersRoute(sessionRepository, logger))
        get("/username", UsernameRoute(sessionRepository, userRepository, logger))
        post("/executeDeposit", DepositRoute(sessionRepository, accountRepository, logger))
        post("/executeWithdraw", WithdrawRoute(sessionRepository, accountRepository, logger))
        post("/newAccount", NewAccountRoute(sessionRepository, accountRepository, logger))
        delete("/removeAccount", RemoveAccountRoute(sessionRepository, accountRepository, logger))
    }
    get("/*") { _, res -> res.redirect("/home") }
}

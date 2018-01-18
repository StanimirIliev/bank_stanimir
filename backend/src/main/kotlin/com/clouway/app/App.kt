package com.clouway.app

import com.clouway.app.adapter.http.Secured
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
import java.io.FileReader
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

    val config = Configuration(Configuration.VERSION_2_3_23)
    config.setDirectoryForTemplateLoading(File("templates"))
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

    val sessionRepository = JdbcSessionRepository(jdbcTemplate)
    val transactionRepository = JdbcTransactionRepository(jdbcTemplate)
    val accountRepository = JdbcAccountRepository(jdbcTemplate, transactionRepository)
    val userRepository = JdbcUserRepository(jdbcTemplate)
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

    initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
    internalServerError { _, res ->
        res.redirect("/static/index/images/500-wallpaper.jpg")
    }

    val transformer = JsonTransformer()

    port(8080)

    get("/static/*", StaticFilesRoute())
    get("/index", IndexPageRoute())
    post("/login", LoginUserHandler(userRepository, sessionRepository, config))
    get("/login", LoginPageRoute(config))
    get("/registration", RegistrationPageRoute(config))
    post("/registration", RegisterUserHandler(userRepository, sessionRepository, compositeValidator, config))
    get("/home", Secured(sessionRepository, HomePageRoute(), logger))
    get("/logout", LogoutRoute(sessionRepository, logger), transformer)
    path("/v1") {
        path("/accounts") {
            get("", Secured(sessionRepository, AccountsListRoute(accountRepository), logger), transformer)
            get("/:id", Secured(sessionRepository, AccountDetailsRoute(accountRepository), logger), transformer)
            post("", Secured(sessionRepository, NewAccountRoute(accountRepository), logger), transformer)
            post("/:id/deposit", Secured(sessionRepository, DepositRoute(accountRepository), logger), transformer)
            post("/:id/withdraw", Secured(sessionRepository, WithdrawRoute(accountRepository), logger), transformer)
            delete("/:id", Secured(sessionRepository, RemoveAccountRoute(accountRepository, logger), logger), transformer)
        }
        get("/activity", ActivityRoute(sessionRepository), transformer)
        get("/username", Secured(sessionRepository, UsersRoute(userRepository), logger), transformer)
        get("/transactions/:param", Secured(sessionRepository, TransactionsRoute(transactionRepository), logger), transformer)
    }
    get("/*") { _, res -> res.redirect("/home") }
	Thread(InactiveSessionsRemover(
    	5,
        TimeUnit.SECONDS,
        sessionRepository,
        Logger.getLogger("InactiveSessionsRemover")
    )).start()

}

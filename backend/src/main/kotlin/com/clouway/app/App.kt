package com.clouway.app

import com.clouway.app.adapter.datastore.DatastoreAccountRepository
import com.clouway.app.adapter.datastore.DatastoreSessionRepository
import com.clouway.app.adapter.datastore.DatastoreTransactionRepository
import com.clouway.app.adapter.datastore.DatastoreUserRepository
import com.clouway.app.adapter.http.Secured
import com.clouway.app.adapter.http.delete.RemoveAccountRoute
import com.clouway.app.adapter.http.get.*
import com.clouway.app.adapter.http.post.*
import com.clouway.app.datastore.NoSqlDatastoreTemplate
import com.google.appengine.api.datastore.DatastoreService
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark
import spark.Spark.*
import java.io.File
import java.util.concurrent.TimeUnit

class App(private val datastore: DatastoreService) {
    fun start() {
        val config = Configuration(Configuration.VERSION_2_3_23)
        config.setDirectoryForTemplateLoading(File("freemarker/templates/"))
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false

        val datastoreTemplate = NoSqlDatastoreTemplate(datastore)

        val sessionRepository = DatastoreSessionRepository(datastoreTemplate)
        val transactionRepository = DatastoreTransactionRepository(datastoreTemplate)
        val accountRepository = DatastoreAccountRepository(datastoreTemplate, transactionRepository)
        val userRepository = DatastoreUserRepository(datastoreTemplate)
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
            get("/transactions/:param", Secured(sessionRepository, TransactionsRoute(transactionRepository,
                    accountRepository), logger), transformer)
        }
        get("/*") { _, res -> res.redirect("/home") }
        Thread(InactiveSessionsRemover(
                1,
                TimeUnit.HOURS,
                sessionRepository,
                Logger.getLogger("InactiveSessionsRemover")
        )).start()
        awaitInitialization()
    }

    fun stop() {
        Spark.stop()
    }
}

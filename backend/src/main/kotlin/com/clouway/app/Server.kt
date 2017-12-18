package com.clouway.app

import com.clouway.app.adapter.jdbc.JdbcAccountRepository
import com.clouway.app.adapter.jdbc.JdbcSessionRepository
import com.clouway.app.adapter.jdbc.JdbcTransactionRepository
import com.clouway.app.adapter.jdbc.JdbcUserRepository
import com.mysql.cj.jdbc.MysqlDataSource
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import java.io.File
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener


class Server(port: Int) {
    private val server = Server(port)
    private val config = Configuration(Configuration.VERSION_2_3_23)
    private val sessionsTable = "Sessions"
    private val usersTable = "Users"
    private val accountsTable = "Accounts"
    private val transactionsTable = "Transactions"

    fun start() {
        config.setDirectoryForTemplateLoading(File("backend/freemarker/templates"))
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)

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
                RegexValidationRule("userId", "[a-zA-Z\\d]{4,15}",
                        "Incorrect userId.\nShould be between 4 and 15 characters long " +
                                "and to not contain special symbols.\n"),
                RegexValidationRule("password", "^[a-zA-Z\\d]{6,30}\$",
                        "Incorrect password.\nShould be between 6 and 30 characters long, " +
                                "and to not contain special symbols."))

        val loginServlet = LoginServlet(userRepository, sessionRepository, config)
        val registrationServlet = RegistrationServlet(userRepository, sessionRepository, compositeValidator, config)
        val indexServlet = IndexServlet()
        val homePageServlet = HomePageServlet(sessionRepository)
        val resourceServlet = ResourceServlet()
        val restServlet = RESTServlet(sessionRepository, accountRepository)
        val staticFilesServlet = StaticFilesServlet()

        context.contextPath = "/"
        context.addEventListener(object : ServletContextListener {
            override fun contextInitialized(sce: ServletContextEvent) {
                val servletContext = sce.servletContext
                servletContext.addServlet("Login page", loginServlet).addMapping("/login")
                servletContext.addServlet("Registration page", registrationServlet).addMapping("/registration")
                servletContext.addServlet("Index page", indexServlet).addMapping("/index")
                servletContext.addServlet("Home page", homePageServlet).addMapping("/home")
                servletContext.addServlet("Resources", resourceServlet).addMapping("/assets/*")
                servletContext.addServlet("REST Servlet", restServlet).addMapping("/v1/*")
                servletContext.addServlet("Static Files", staticFilesServlet).addMapping("/static/*")
            }

            override fun contextDestroyed(sce: ServletContextEvent?) {

            }
        })
        server.handler = context
        try {
            server.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

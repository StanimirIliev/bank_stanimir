package com.clouway.app

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

    fun start() {
        config.setDirectoryForTemplateLoading(File("freemarker/templates"))
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)

        val mySqlDataSource = MysqlDataSource()
        mySqlDataSource.setUrl("jdbc:mysql://${System.getenv("DB_HOST")}/${System.getenv("DB_TABLE")}")
        mySqlDataSource.user = System.getenv("DB_USER")
        mySqlDataSource.setPassword(System.getenv("DB_PASS"))

        context.contextPath = "/"
        context.addEventListener(object : ServletContextListener {
            override fun contextInitialized(sce: ServletContextEvent) {
                val servletContext = sce.servletContext
                servletContext.addServlet("Login page",
                        LoginServlet(JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), "Users"), config))
                        .addMapping("/login")
                servletContext.addServlet("Registration page", RegistrationServlet(
                        CompositeValidator(RegexValidationRule("username", "[a-zA-Z\\d]{4,15}",
                        "Incorrect username.\nShould be between 4 and 15 characters long " +
                                "and to not contain special symbols.\n"),
                        RegexValidationRule("password", "^[a-zA-Z\\d]{6,30}\$",
                                "Incorrect password.\nShould be between 6 and 30 characters long, " +
                                        "and to not contain special symbols.")),
                        JdbcUserRepository(MySQLJdbcTemplate(mySqlDataSource), "Users"), config))
                        .addMapping("/registration")
                servletContext.addServlet("Index page", IndexServlet())
                        .addMapping("/index")
                servletContext.addServlet("Successful Registration", SuccessfulAuthorizationServlet())
                        .addMapping("/home")
                servletContext.addServlet("Resources", ResourceServlet())
                        .addMapping("/assets/*")
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
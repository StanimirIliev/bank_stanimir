package com.clouway.app.adapter.http.post

import com.clouway.app.core.Error
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.sql.Timestamp
import java.time.LocalDateTime

class LoginPageRoutePost(private val userRepository: UserRepository, private val sessionRepository: SessionRepository,
                         private val config: Configuration) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("login.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        if (!userRepository.authenticate(req.queryParams("username"), req.queryParams("password"))) {
            template.process(Error("Wrong userId or password"), out)
            return out.toString()
        }
        val userId = userRepository.getUserId(req.queryParams("username"))
        val session = req.session(true)
        session.maxInactiveInterval(2 * 60 * 60)// two hours
        session.attribute("userId", userId)
        val sessionId = sessionRepository.registerSession(Session(userId,
                Timestamp.valueOf(LocalDateTime.now().plusHours(8))))
        resp.cookie("sessionId", sessionId)
        return resp.redirect("/home")
    }
}
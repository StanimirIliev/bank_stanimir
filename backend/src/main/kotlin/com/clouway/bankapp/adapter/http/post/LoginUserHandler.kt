package com.clouway.bankapp.adapter.http.post

import com.clouway.bankapp.core.Error
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionRepository
import com.clouway.bankapp.core.UserRepository
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime

class LoginUserHandler(private val userRepository: UserRepository, private val sessionRepository: SessionRepository,
                       private val config: Configuration) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("login.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        if (!userRepository.authenticate(req.queryParams("username"), req.queryParams("password"))) {
            template.process(Error("Wrong username or password"), out)
            return out.toString()
        }
        val userId = userRepository.getUserId(req.queryParams("username"))
        val sessionId = sessionRepository.registerSession(Session(
                userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        ))
        resp.cookie("sessionId", sessionId)
        return resp.redirect("/home")
    }
}
package com.clouway.app.adapter.http.post

import com.clouway.app.core.*
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

class RegistrationPageRoutePost(private val userRepository: UserRepository,
                                private val sessionRepository: SessionRepository,
                                private val validator: RequestValidator,
                                private val config: Configuration) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val dataModel = HashMap<String, List<Error>>()
        val template = config.getTemplate("registration.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val errorList = validator.validate(req.queryMap().toMap())
        when {
            !errorList.isEmpty() -> {
                dataModel.put("errors", errorList)
                template.process(dataModel.apply { put("errors", errorList) }, out)
                return out.toString()
            }
            req.queryParams("password") != req.queryParams("confirmPassword") -> {
                dataModel.put("errors", listOf(Error("The password and the confirm password does not match.")))
                template.process(dataModel.apply {
                    put("errors", listOf(Error("The password and the " +
                            "confirm password does not match.")))
                }, out)
                return out.toString()
            }
            userRepository.registerUser(req.queryParams("username"),
                    req.queryParams("password")) == -1 -> {
                template.process(dataModel.apply {
                    put("errors",
                            listOf(Error("This username is already taken")))
                }, out)
                return out.toString()
            }
            else -> {
                val userId = userRepository.getUserId(req.queryParams("username"))
                val session = req.session(true)
                session.maxInactiveInterval(2 * 60 * 60)// two hours
                session.attribute("userId", userId)
                val sessionId = sessionRepository.registerSession(Session(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2)
                ))
                resp.cookie("sessionId", sessionId)
                return resp.redirect("/home")
            }
        }
    }
}
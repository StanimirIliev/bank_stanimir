package com.clouway.app.adapter.http.get

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class UsernameRoute(
        private val sessionRepository: SessionRepository,
        private val userRepository: UserRepository,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {

        resp.type("application/json")
        var session: Session?
        if (req.cookie("sessionId") == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            logger.error("Error occurred while getting the cookie sessionId")
            return "{\"message\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            session = sessionRepository.getSessionAvailableAt(req.cookie("sessionId"), LocalDateTime.now())
            if (session == null) {
                resp.status(HttpStatus.BAD_REQUEST_400)
                logger.error("Invalid sessionId")
                return "{\"message\":\"Invalid sessionId\"}"
            }
        }

        val username = userRepository.getUsername(session.userId)
        return if (username == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            "{\"message\":\"Cannot get username. Invalid userId.\"}"
        } else {
            "{\"username\":\"$username\"}"
        }
    }
}
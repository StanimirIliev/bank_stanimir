package com.clouway.app.adapter.http.get

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class ActiveUsersRoute(
        private val sessionRepository: SessionRepository,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {

        resp.type("application/json")
        var session: Session?
        if (req.cookie("sessionId") == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            logger.error("Error occurred while getting the cookie sessionId")
            return "{\"msg\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            session = sessionRepository.getSession(req.cookie("sessionId"))
            if (session == null) {
                resp.status(HttpStatus.BAD_REQUEST_400)
                logger.error("Invalid sessionId")
                return "{\"msg\":\"Invalid sessionId\"}"
            }
        }

        return "{\"activeUsers\":${sessionRepository.getSessionsCount()}}"
    }
}
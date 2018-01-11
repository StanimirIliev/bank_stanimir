package com.clouway.app.adapter.http.get

import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class LogoutRoute(private val sessionRepository: SessionRepository, private val logger: Logger) : Route {
    override fun handle(req: Request, resp: Response): Any {
        return if (req.cookie("sessionId") == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            logger.error("Error occurred while getting the cookie sessionId")
            "{\"msg\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            if (!sessionRepository.deleteSession(req.cookie("sessionId"))) {
                resp.type("application/json")
                resp.status(HttpStatus.BAD_REQUEST_400)
                "{\"msg\":\"Error occurred while getting the cookie sessionId\"}"
            } else {
                req.session().invalidate()
                resp.redirect("/index")
            }
        }
    }
}
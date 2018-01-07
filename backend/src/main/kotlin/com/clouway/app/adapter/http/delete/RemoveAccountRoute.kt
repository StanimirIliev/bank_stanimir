package com.clouway.app.adapter.http.delete

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class RemoveAccountRoute(
        private val sessionRepository: SessionRepository,
        private val accountRepository: AccountRepository,
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

        val accountId = req.queryParams("id")
        when {
            accountId == null -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"msg\":\"Cannot remove this account. No account id passed with " +
                        "the request\"}"
            }
            !accountRepository.authenticate(accountId.toInt(), session.userId) -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"msg\":\"Cannot remove this account. Access denied.\"}"
            }
            accountRepository.removeAccount(accountId.toInt()) -> {
                resp.status(HttpStatus.OK_200)
                return "{\"msg\":\"This account has been removed successfully.\"}"
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                return "{\"msg\":\"Cannot remove this account.\"}"
            }
        }
    }
}
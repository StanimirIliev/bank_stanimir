package com.clouway.app.adapter.http.delete

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

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
            return "{\"message\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            session = sessionRepository.getSessionAvailableAt(req.cookie("sessionId"), LocalDateTime.now())
            if (session == null) {
                resp.status(HttpStatus.BAD_REQUEST_400)
                logger.error("Invalid sessionId")
                return "{\"message\":\"Invalid sessionId\"}"
            }
        }

        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            return "{\"message\":\"Cannot remove this account. No account id passed with " +
                    "the request\"}"
        } else {
            val operationResponse = accountRepository.removeAccount(accountId.toInt(), session.userId)

            if (operationResponse.successful) {
                resp.status(HttpStatus.OK_200)
                return "{\"message\":\"${"This account has been removed successfully."}\"}"
            }
            if (operationResponse.message == "account-not-exist") {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"message\":\"This account does not exist.\"}"
            }
            resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            logger.fatal("Error occurred while removing account $accountId, requested by user ${session.userId}")
            return "{\"message\":\"Error occurred while removing this account.\"}"
        }
    }
}
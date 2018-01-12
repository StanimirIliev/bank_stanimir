package com.clouway.app.adapter.http.get

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.google.gson.Gson
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class AccountDetailsRoute(
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
            return "{\"message\":\"Cannot get account. No account id passed with the request.\"}"
        } else {
            val account = accountRepository.getUserAccount(session.userId, accountId.toInt())
            if (account == null) {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"message\":\"Account not found.\"}"
            }
            return "{\"account\":${Gson().toJson(account)}}"
        }
    }
}
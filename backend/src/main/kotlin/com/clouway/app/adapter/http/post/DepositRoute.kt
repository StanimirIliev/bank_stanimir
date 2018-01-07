package com.clouway.app.adapter.http.post

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.google.gson.Gson
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class DepositRoute(
        private val sessionRepository: SessionRepository,
        private val accountRepository: AccountRepository,
        private val logger: Logger
) : Route {

    data class Params(val id: Int?, val value: Float?)
    data class Wrapper(val params: Params)

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
        val data1 = req.body()
        val params = req.params()
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val accountId = data.params.id
        val amount = data.params.value
        when {
            accountId == null || amount == null -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"msg\":\"Cannot execute this deposit. No account id or value passed with " +
                        "the request\"}"
            }
            !accountRepository.authenticate(accountId, session.userId) -> {
                resp.status(HttpStatus.UNAUTHORIZED_401)
                return "{\"msg\":\"Cannot execute this deposit. Access denied.\"}"
            }
            accountRepository.updateBalance(accountId, amount) -> {
                resp.status(HttpStatus.CREATED_201)
                return "{\"msg\":\"Deposit successful.\"}"
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                return "{\"msg\":\"Deposit unsuccessful.\"}"
            }
        }
    }
}
package com.clouway.app.adapter.http.post

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class DepositRoute(
        private val accountRepository: AccountRepository,
        var session: Session
) : Route {

    data class Params(val value: Float?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val accountId = req.params("id").toIntOrNull()
        val amount = data?.params?.value
        if (accountId == null || amount == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            return "{\"message\":\"Cannot execute this deposit. No account id or value passed with the request\"}"
        }
        val operationResponse = accountRepository.updateBalance(accountId, session.userId, amount)
        if (operationResponse.successful) {
            resp.status(HttpStatus.CREATED_201)
            return "{\"message\":\"Deposit successful.\"}"
        }
        return when (operationResponse.message) {
            "incorrect-id" -> {
                resp.status(HttpStatus.NOT_FOUND_404)
                "{\"message\":\"Account not found.\"}"
            }
            "invalid-request" -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                "{\"message\":\"Invalid request.\"}"
            }
            "access-denied" -> {
                resp.status(HttpStatus.UNAUTHORIZED_401)
                "{\"message\":\"Cannot execute this deposit. Access denied.\"}"
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                "{\"message\":\"Error occurred while executing the deposit.\"}"
            }
        }
    }
}
package com.clouway.app.adapter.http.post

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.ErrorType.*
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.httpresponse.HttpError
import com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class WithdrawRoute(private val accountRepository: AccountRepository) : SecuredRoute {

    data class Params(val value: Float?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val accountId = req.params("id").toIntOrNull()
        val amount = data.params.value
        if (accountId == null || amount == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            return HttpError("Error. Id or amount parameter not passed.")
        }
        val operationResponse = accountRepository.updateBalance(accountId, session.userId, amount * -1f)
        if (operationResponse.isSuccessful) {
            resp.status(HttpStatus.CREATED_201)
            return GetMessageResponseDto("Withdraw successful.")
        }
        return when (operationResponse.error) {
            INCORRECT_ID -> {
                resp.status(HttpStatus.NOT_FOUND_404)
                HttpError("Account not found.")
            }
            INVALID_REQUEST -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                HttpError("Invalid request.")
            }
            LOW_BALANCE -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                HttpError("Cannot execute this withdraw. Not enough balance.")
            }
            ACCESS_DENIED -> {
                resp.status(HttpStatus.UNAUTHORIZED_401)
                HttpError("Cannot execute this withdraw. Access denied.")
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                HttpError("Error occurred while executing the deposit.")
            }
        }
    }
}
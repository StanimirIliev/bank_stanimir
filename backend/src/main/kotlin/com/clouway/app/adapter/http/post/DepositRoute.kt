package com.clouway.app.adapter.http.post

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import com.clouway.app.core.ErrorType.*
import com.clouway.app.core.httpresponse.GetMessageResponseDto

class DepositRoute(private val accountRepository: AccountRepository) : SecuredRoute {

    data class Params(val value: Float?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val accountId = req.params("id").toIntOrNull()
        val amount = data?.params?.value
        if (accountId == null || amount == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            return GetMessageResponseDto("Cannot execute this deposit. No account id or value passed with the request.")
        }
        val operationResponse = accountRepository.updateBalance(accountId, session.userId, amount)
        if (operationResponse.isSuccessful) {
            resp.status(HttpStatus.CREATED_201)
            return GetMessageResponseDto("Deposit successful.")
        }
        return when (operationResponse.error) {
            INCORRECT_ID -> {
                resp.status(HttpStatus.NOT_FOUND_404)
                GetMessageResponseDto("Account not found.")
            }
            INVALID_REQUEST -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                GetMessageResponseDto("Invalid request.")
            }
            ACCESS_DENIED -> {
                resp.status(HttpStatus.UNAUTHORIZED_401)
                GetMessageResponseDto("Cannot execute this deposit. Access denied.")
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                GetMessageResponseDto("Error occurred while executing the deposit.")
            }
        }
    }
}
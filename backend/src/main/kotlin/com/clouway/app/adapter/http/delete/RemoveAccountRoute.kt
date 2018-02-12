package com.clouway.app.adapter.http.delete

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.ErrorType.ACCOUNT_NOT_FOUND
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class RemoveAccountRoute(
        private val accountRepository: AccountRepository,
        private val logger: Logger
) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            return GetMessageResponseDto("Cannot remove this account. No account id passed with the request.")
        } else {
            val operationResponse = accountRepository.removeAccount(accountId.toInt(), session.userId)

            if (operationResponse.isSuccessful) {
                resp.status(HttpStatus.OK_200)
                return GetMessageResponseDto("This account has been removed successfully.")
            }
            if (operationResponse.error == ACCOUNT_NOT_FOUND) {
                resp.status(HttpStatus.NOT_FOUND_404)
                return GetMessageResponseDto("Account not found.")
            }
            resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            logger.fatal("Error occurred while removing account $accountId, requested by user ${session.userId}")
            return GetMessageResponseDto("Error occurred while removing this account.")
        }
    }
}
package com.clouway.app.adapter.http.delete

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import org.apache.log4j.Logger
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class RemoveAccountRoute(
        private val accountRepository: AccountRepository,
        var session: Session,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {
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
            if (operationResponse.message == "account-not-found") {
                resp.status(HttpStatus.NOT_FOUND_404)
                return "{\"message\":\"Account not found.\"}"
            }
            resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            logger.fatal("Error occurred while removing account $accountId, requested by user ${session.userId}")
            return "{\"message\":\"Error occurred while removing this account.\"}"
        }
    }
}
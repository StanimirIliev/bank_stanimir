package com.clouway.app.adapter.http.post

import com.clouway.app.core.*
import com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class NewAccountRoute(private val accountRepository: AccountRepository) : SecuredRoute {

    data class Params(val title: String?, val currency: Currency?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val title = data.params.title
        val currency = data.params.currency
        val accounts = accountRepository.getAllAccounts(session.userId)
        when {
            title == null || currency == null -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"message\":\"Cannot open new account. No title or currency passed with " +
                        "the request\"}"
            }
            accounts.any { it.title == title } -> {
                resp.status(HttpStatus.BAD_REQUEST_400)
                return "{\"message\":\"You have already account with such a title.\"}"
            }
            accountRepository.registerAccount(Account(title, session.userId, currency, 0f)) != -1 -> {
                resp.status(HttpStatus.CREATED_201)
                return "{\"message\":\"New account opened successful.\"}"
            }
            else -> {
                resp.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                return "{\"message\":\"Unable to open new account.\"}"
            }
        }
    }
}
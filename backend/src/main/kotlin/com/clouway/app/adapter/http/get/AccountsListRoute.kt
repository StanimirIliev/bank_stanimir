package com.clouway.app.adapter.http.get

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.google.gson.Gson
import spark.Request
import spark.Response
import spark.Route

class AccountsListRoute(
        private val accountRepository: AccountRepository,
        var session: Session
) : Route {
    override fun handle(req: Request, resp: Response): Any =
            "{\"content\":${Gson().toJson(accountRepository.getAllAccounts(session.userId))}}"
}


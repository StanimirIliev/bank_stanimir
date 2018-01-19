package com.clouway.bankapp.adapter.http.get

import com.clouway.bankapp.core.AccountRepository
import com.clouway.bankapp.core.SecuredRoute
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.httpresponse.HttpResponseAccountsList
import spark.Request
import spark.Response

class AccountsListRoute(private val accountRepository: AccountRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any =
            HttpResponseAccountsList(accountRepository.getAllAccounts(session.userId))
}


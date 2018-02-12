package com.clouway.app.adapter.http.get

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.httpresponse.GetAccountListResponseDto
import spark.Request
import spark.Response

class AccountsListRoute(private val accountRepository: AccountRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any =
            GetAccountListResponseDto(accountRepository.getAllAccounts(session.userId))
}


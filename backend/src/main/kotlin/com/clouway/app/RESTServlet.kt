package com.clouway.app

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RESTServlet(private val sessionRepository: SessionRepository,
                  private val accountRepository: AccountRepository) :
        HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val uri = req.requestURI
        var session: Session?
        if (!uri.contains("/")) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        val parameter = uri.substring(uri.indexOf("/", uri.indexOf("v1")) + 1)
        resp.contentType = "application/json"

        val cookie: Cookie? = req.cookies.find { it.name == "id" }
        if (cookie == null) {
            resp.writer.print("{\"error\":\"Error occurred while getting cookie\"}")
            throw IllegalArgumentException("Error occurred while getting cookie")
        } else {
            session = sessionRepository.getSession(cookie.value)
            if (session == null) {
                resp.writer.print("{\"error\":\"Error occurred while getting session\"}")
                throw IllegalArgumentException("Error occurred while getting session")
            }
        }

        if (parameter == "balance") {
            val accountTitle = req.getParameter("accountTitle")
            if (accountTitle == null) {
                resp.writer.print("{\"error\":\"Could not find account title parameter\"}")
                throw IllegalArgumentException("Could not find account title parameter")
            }
            val accountId = accountRepository.getAccountId(accountTitle, session.userId)
            if (accountId == null) {
                resp.writer.print("{\"error\":\"Error occurred while getting account id\"}")
                throw IllegalArgumentException("Error occurred while getting account id")
            }
            val balance: Float? = accountRepository.getBalance(accountId)
            if (balance == null) {
                resp.writer.print("{\"error\":\"Error occurred while getting balance\"}")
                throw IllegalArgumentException("Error occurred while getting balance")
            } else {
                resp.writer.print("{\"balance\":$balance}")
            }
        } else if (parameter == "userId") {
            resp.writer.print("{\"userId\":\"${session.userId}\"}")
        } else if (parameter == "activeUsers") {
            resp.writer.print("{\"activeUsers\":${sessionRepository.getSessionsCount()}}")
        }
    }
}

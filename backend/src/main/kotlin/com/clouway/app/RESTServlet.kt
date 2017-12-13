package com.clouway.app

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RESTServlet(private val userRepository: UserRepository, private val sessionRepository: SessionRepository):
        HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val uri = req.requestURI
        var session: Session?
        if(!uri.contains("/")) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        val parameter = uri.substring(uri.indexOf("/", uri.indexOf("v1")) + 1)
        resp.contentType = "application/json"

        val cookie: Cookie? = req.cookies.find { it.name == "sessionId" }
        if(cookie == null) {
            error(resp)
            return
        }
        else {
            session = sessionRepository.getSession(cookie.value)
            if(session == null) {
                error(resp)
                return
            }
        }

        if(parameter == "balance") {
            val balance: Float? = userRepository.getBalance(session.username)
            if(balance == null) {
                error(resp)
            }
            else {
                resp.writer.print("{\"balance\":$balance}")
            }
        }
        else if(parameter == "username") {
            resp.writer.print("{\"username\":\"${session.username}\"}")
        }
        else if(parameter == "activeUsers") {
            resp.writer.print("{\"activeUsers\":${sessionRepository.getSessionsCount()}}")
        }
    }

    private fun error(resp: HttpServletResponse) {
        resp.writer.print("{\"balance\":\"error\"")
    }
}

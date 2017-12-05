package com.clouway.app

import com.clouway.app.core.UserRepository
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RESTServlet(private val userRepository: UserRepository, private val sessionCounter: SessionCounter): HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val uri = req.requestURI
        if(!uri.contains("/")) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        val parameter = uri.substring(uri.indexOf("/", uri.indexOf("v1")) + 1)
        resp.contentType = "application/json"
        if(parameter == "balance") {
            val cookies = req.cookies
            var username = ""
            var password = ""
            cookies.filter{it.name == "username"}.forEach { username = it.value }
            cookies.filter{it.name == "password"}.forEach { password = it.value }
            resp.writer.print("{\"balance\":${userRepository.getBalance(username, password, true)}}")
        }
        else if(parameter == "activeUsers") {
            resp.writer.print("{\"activeUsers\":${sessionCounter.getActiveSessionNumber()}}")
        }
    }
}
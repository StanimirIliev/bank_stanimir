package com.clouway.app

import com.clouway.app.core.User
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SuccessfulAuthorizationServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val out = resp.writer
        resp.contentType = "text/html"
        val user = req.session.getAttribute("user") as User
        out.print("<h1>Hello ${user.username}</h1>")

    }
}
package com.clouway.app

import java.nio.charset.Charset
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SuccessfulAuthorizationServlet(private val sessionCounter: SessionCounter) : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.session.getAttribute("user") != null) {
            if(req.getParameter("logout") != null) {
                sessionCounter.sessionDestroyed(req.session.id)
                req.session.invalidate()
                resp.addCookie(Cookie("username", ""))
                resp.addCookie(Cookie("password", ""))
                resp.sendRedirect("/index")
            }
            else {
                resp.contentType = "text/html"
                val index = SuccessfulAuthorizationServlet::class.java.getResourceAsStream("/react/index.html")
                        .readBytes().toString(Charset.defaultCharset())
                resp.writer.print(index)
            }

        } else {
            resp.addCookie(Cookie("username", ""))
            resp.addCookie(Cookie("password", ""))
            resp.sendRedirect("/index")
        }
    }
}

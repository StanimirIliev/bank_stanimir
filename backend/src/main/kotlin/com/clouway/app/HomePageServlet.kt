package com.clouway.app

import com.clouway.app.core.SessionRepository
import java.io.File
import java.io.FileReader
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HomePageServlet(private val sessionRepository: SessionRepository) : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.session.getAttribute("username") != null) {
            if (req.getParameter("logout") != null) {
                val cookie: Cookie? = req.cookies.find { it.name == "sessionId" }
                if (cookie == null) {
                    resp.contentType = "text/html"
                    resp.sendError(HttpServletResponse.SC_CONFLICT,
                            "Cookie with name 'sessionId' not found. Unable to delete session from DB.")
                } else {
                    if (!sessionRepository.deleteSession(cookie.value)) {
                        resp.contentType = "text/html"
                        resp.sendError(HttpServletResponse.SC_CONFLICT,
                                "The value of the cookie with name 'sessionId' was changed. Unable to delete session from DB.")
                    } else {
                        req.session.invalidate()
                        resp.sendRedirect("/index")
                    }
                }
            } else {
                resp.contentType = "text/html"
                val jarLocation = File(HomePageServlet::class.java.protectionDomain.codeSource.location.path)
                val htmlLocation = jarLocation.parentFile.absolutePath + "/index.html"
                resp.writer.print(FileReader(htmlLocation).readText())
            }

        } else {
            resp.sendRedirect("/index")
        }
    }
}
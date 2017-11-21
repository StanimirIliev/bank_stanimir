package com.clouway.app

import com.clouway.app.core.UserRepository
import freemarker.template.Configuration
import freemarker.template.Template
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginServlet(private val userRepo: UserRepository, private val config: Configuration) : HttpServlet() {
                   private val sessionCounter: SessionCounter) : HttpServlet() {

    lateinit var template: Template

    override fun init() {
        template = config.getTemplate("login.ftlh")
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html; charset=utf-8"
        val out = resp.writer
        template.process(Error(""), out)
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html; charset=utf-8"
        val out = resp.writer
        val user: User? = userRepo.authenticate(req.getParameter("username"), req.getParameter("password"))
        if (user == null) {
            template.process(Error("Wrong username or password"), out)
            return
        }
        val session = req.getSession(true)
        session.maxInactiveInterval = 2*60*60// two hours
        session.setAttribute("user", user)
        resp.sendRedirect("/home")
    }
}


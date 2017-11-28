package com.clouway.app

import com.clouway.app.core.RequestValidator
import com.clouway.app.core.UserRepository
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RegistrationServlet(private val validator: RequestValidator, private val userRepo: UserRepository,
                          private val config: Configuration) : HttpServlet() {

    private val dataModel = HashMap<String, List<Error>>()
    lateinit var template: Template

    override fun init() {
        template = config.getTemplate("register.ftlh")
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html; charset=utf-8"
        val out = resp.writer
        template.process(dataModel.apply { put("errors", listOf()) }, out)
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html; charset=utf-8"
        val out = resp.writer
        val errorList = validator.validate(req.parameterMap)
        if(!errorList.isEmpty()) {
            dataModel.put("errors", errorList)
            template.process(dataModel.apply { put("errors", errorList) }, out)
            return
        }
        val user = userRepo.registerUser(req.getParameter("username"), req.getParameter("password"))
        if (user == null) {
            template.process(dataModel.apply { put("errors",
                    listOf(Error("This username is already taken"))) }, out)
        } else {
            val session = req.getSession(true)
            session.maxInactiveInterval = 2*60*60// two hours
            session.setAttribute("user", user)
            resp.sendRedirect("/home")
        }
    }
}
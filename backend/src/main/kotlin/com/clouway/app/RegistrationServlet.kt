package com.clouway.app

import com.clouway.app.core.*
import freemarker.template.Configuration
import freemarker.template.Template
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RegistrationServlet(private val userRepo: UserRepository,
                          private val sessionRepository: SessionRepository,
                          private val validator: RequestValidator,
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
            session.setAttribute("username", user.username)
            var sessionId = getRandomString(20)
            while(sessionRepository.getSession(sessionId) != null) {
                sessionId = getRandomString(20)
            }
            resp.addCookie(Cookie("sessionId", sessionId))
            sessionRepository.registerSession(Session(sessionId, user.username,
                    Timestamp.valueOf(LocalDateTime.now().plusHours(8))))
            resp.sendRedirect("/home")
        }
    }

    private fun getRandomString(size: Int): String {
        val random = Random()
        val numbers = IntArray(size)
        val result = StringBuilder()
        for (i in numbers.indices) {// 48-57 digits 65-90 uppercase letters 97-122 lowercase letters
            numbers[i] = random.nextInt(74) + 48
            while (numbers[i] in 58..64 || numbers[i] in 91..96) {
                numbers[i] = random.nextInt(74) + 48
            }
        }
        numbers.forEach { result.append(it.toChar()) }.toString()
        return result.toString()
    }
}


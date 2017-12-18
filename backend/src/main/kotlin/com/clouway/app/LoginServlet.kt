package com.clouway.app

import com.clouway.app.core.Error
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import freemarker.template.Configuration
import freemarker.template.Template
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginServlet(private val userRepository: UserRepository, private val sessionRepository: SessionRepository,
                   private val config: Configuration) : HttpServlet() {

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
        if (!userRepository.authenticate(req.getParameter("username"), req.getParameter("password"))) {
            template.process(Error("Wrong userId or password"), out)
            return
        }
        val userId = userRepository.getUserId(req.getParameter("username"), req.getParameter("password"))
        if (userId == null) {
            template.process(Error("Error occurred while getting userId"), out)
            return
        }
        val session = req.getSession(true)
        session.maxInactiveInterval = 2 * 60 * 60// two hours
        session.setAttribute("userId", userId)
        var sessionId = getRandomString(20)
        while (sessionRepository.getSession(sessionId) != null) {
            sessionId = getRandomString(20)
        }
        resp.addCookie(Cookie("id", sessionId))
        sessionRepository.registerSession(Session(sessionId, userId,
                Timestamp.valueOf(LocalDateTime.now().plusHours(8))))
        resp.sendRedirect("/home")
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
        numbers.forEach { result.append(it.toChar()) }
        return result.toString()
    }
}

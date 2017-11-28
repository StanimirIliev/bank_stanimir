package com.clouway.app

import java.nio.charset.Charset
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class IndexServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html"
        resp.writer.print(IndexServlet::class.java.getResourceAsStream("/index.html")
                .readBytes().toString(Charset.defaultCharset()))
    }
}
package com.clouway.app

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ResourceServlet: HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val uri = req.requestURI
        if(!uri.contains("/")) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        val fileName = uri.substring(uri.indexOf("/", uri.indexOf("assets")))
        val input = ResourceServlet::class.java.getResourceAsStream(fileName)
        if(input == null) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        when {
            fileName.endsWith(".css") -> resp.contentType = "text/css"
            fileName.endsWith(".js") -> resp.contentType = "application/javascript"
        }
        input.copyTo(resp.outputStream)
    }
}
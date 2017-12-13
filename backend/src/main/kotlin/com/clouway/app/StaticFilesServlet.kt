package com.clouway.app

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class StaticFilesServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val jarLocation = File(HomePageServlet::class.java.protectionDomain.codeSource.location.path)
        val staticDirLocation = jarLocation.parentFile.absolutePath + "/static"
        val uri = req.requestURI
        if(!uri.contains("/")) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        val fileName = uri.substring(uri.indexOf("/", uri.indexOf("static")))
        try {
            val input = FileInputStream(staticDirLocation + fileName)
            when {
                fileName.endsWith(".css") -> resp.contentType = "text/css"
                fileName.endsWith(".js") -> resp.contentType = "application/javascript"
            }
            input.copyTo(resp.outputStream)
        }
        catch (e: FileNotFoundException) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
    }
}

package com.clouway.app.adapter.http.get

import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class StaticFilesRoute : Route {
    override fun handle(req: Request, resp: Response): Any {
        val jarLocation = File(StaticFilesRoute::class.java.protectionDomain.codeSource.location.path)
        val staticDirLocation = jarLocation.parentFile.absolutePath + "/static"
        val uri = req.uri()
        val fileName = uri.substring(uri.indexOf("/", uri.indexOf("static")))
        return try {
            val fullPath = staticDirLocation + fileName
            val input = FileInputStream(fullPath)
            when {
                fileName.endsWith(".css") -> resp.type("text/css")
                fileName.endsWith(".js") -> resp.type("application/javascript")
                fileName.endsWith(".png") -> resp.type("image/png")
                fileName.endsWith(".jpg") -> resp.type("image/jpeg")
            }
            input.copyTo(resp.raw().outputStream)
        } catch (e: FileNotFoundException) {
            resp.status(HttpStatus.NOT_FOUND_404)
            resp.type("text/html")
            resp.body("<h1>Resource not found</h1")
        }
    }
}
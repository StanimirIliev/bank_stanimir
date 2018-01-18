package com.clouway.app.adapter.http.get

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
            val input = FileInputStream(staticDirLocation + fileName)
            when {
                fileName.endsWith(".css") -> resp.type("text/css")
                fileName.endsWith(".js") -> resp.type("application/javascript")
            }
            input.copyTo(resp.raw().outputStream)
        } catch (e: FileNotFoundException) {
            resp.type("text/html")
            return resp.body("<h1>Resource not found</h1")
        }
    }
}
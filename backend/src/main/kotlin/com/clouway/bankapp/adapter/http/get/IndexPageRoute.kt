package com.clouway.bankapp.adapter.http.get

import spark.Request
import spark.Response
import spark.Route
import java.io.File
import java.io.FileInputStream

class IndexPageRoute : Route {
    override fun handle(request: Request, resp: Response): Any {
        resp.type("text/html")
        val jarLocation = File(StaticFilesRoute::class.java.protectionDomain.codeSource.location.path)
        val staticDirLocation = jarLocation.parentFile.absolutePath + "/static"
        val fileName = "$staticDirLocation/index/index.html"
        val input = FileInputStream(fileName)
        return input.copyTo(resp.raw().outputStream)
    }

}
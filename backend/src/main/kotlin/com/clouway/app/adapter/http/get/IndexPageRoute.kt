package com.clouway.app.adapter.http.get

import spark.Request
import spark.Response
import spark.Route
import java.io.File
import java.nio.charset.Charset

class IndexPageRoute : Route {
    override fun handle(request: Request, resp: Response): Any {
        resp.type("text/html")
        val jarLocation = File(StaticFilesRoute::class.java.protectionDomain.codeSource.location.path)
        val staticDirLocation = jarLocation.parentFile.absolutePath + "/static"
        val fileName = "$staticDirLocation/welcome/index.html"
        return IndexPageRoute::class.java.getResourceAsStream(fileName).readBytes().toString(Charset.defaultCharset())
    }

}
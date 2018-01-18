package com.clouway.app.adapter.http.get

import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import spark.Request
import spark.Response
import java.io.File
import java.io.FileReader

class HomePageRoute : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        resp.type("text/html")
        val jarLocation = File(HomePageRoute::class.java.protectionDomain.codeSource.location.path)
        val htmlLocation = jarLocation.parentFile.absolutePath + "/index.html"
        return FileReader(htmlLocation).readText()
    }
}
package com.clouway.bankapp.adapter.http.get

import com.clouway.bankapp.core.SecuredRoute
import com.clouway.bankapp.core.Session
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
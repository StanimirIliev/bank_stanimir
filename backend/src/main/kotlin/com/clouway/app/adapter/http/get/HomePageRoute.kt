package com.clouway.app.adapter.http.get

import spark.Request
import spark.Response
import spark.Route
import java.io.File
import java.io.FileReader

class HomePageRoute : Route {
    override fun handle(req: Request, resp: Response): Any {
        val session = req.session()
        return if (session.attribute<Any>("userId") != null) {
            resp.type("text/html")
            val jarLocation = File(HomePageRoute::class.java.protectionDomain.codeSource.location.path)
            val htmlLocation = jarLocation.parentFile.absolutePath + "/index.html"
            FileReader(htmlLocation).readText()

        } else {
            resp.redirect("/index")
        }
    }
}
package com.clouway.app.adapter.http.get

import spark.Request
import spark.Response
import spark.Route

class ResourcesRoute : Route {
    override fun handle(request: Request, response: Response): Any {
        val uri = request.uri()
        val fileName = uri.substring(uri.indexOf("/", uri.indexOf("assets")))
        val input = ResourcesRoute::class.java.getResourceAsStream(fileName)
        if (input == null) {
            response.type("image/jpeg")
            val image = ResourcesRoute::class.java.getResourceAsStream("/images/404-wallpaper.jpg")
            return image.copyTo(response.raw().outputStream)
        }
        when {
            fileName.endsWith(".css") -> response.type("text/css")
            fileName.endsWith(".js") -> response.type("application/javascript")
        }
        return input.copyTo(response.raw().outputStream)
    }
}
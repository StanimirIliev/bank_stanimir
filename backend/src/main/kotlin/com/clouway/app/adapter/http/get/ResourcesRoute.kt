package com.clouway.app.adapter.http.get

import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

class ResourcesRoute : Route {
    override fun handle(request: Request, response: Response): Any {
        val uri = request.uri()
        val fileName = uri.substring(uri.indexOf("/", uri.indexOf("assets")))
        val input = ResourcesRoute::class.java.getResourceAsStream(fileName)
        if (input == null) {
            response.type("text/html")
            response.status(HttpStatus.NOT_FOUND_404)
            return "<h1>Resource not found</h1"
        }
        when {
            fileName.endsWith(".css") -> response.type("text/css")
            fileName.endsWith(".js") -> response.type("application/javascript")
        }
        return input.copyTo(response.raw().outputStream)
    }
}
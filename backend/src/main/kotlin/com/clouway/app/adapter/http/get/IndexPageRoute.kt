package com.clouway.app.adapter.http.get

import com.clouway.app.App
import spark.Request
import spark.Response
import spark.Route
import java.nio.charset.Charset

class IndexPageRoute : Route {
    override fun handle(request: Request, resp: Response): Any {
        resp.type("text/html")
        return App::class.java.getResourceAsStream("/index.html")
                .readBytes().toString(Charset.defaultCharset())
    }

}
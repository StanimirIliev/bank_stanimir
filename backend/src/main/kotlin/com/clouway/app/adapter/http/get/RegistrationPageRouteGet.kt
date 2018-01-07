package com.clouway.app.adapter.http.get

import com.clouway.app.core.Error
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.util.*

class RegistrationPageRouteGet(private val config: Configuration) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val dataModel = HashMap<String, List<Error>>()
        val template = config.getTemplate("registration.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        template.process(dataModel.apply { put("errors", listOf()) }, out)
        return out.toString()
    }
}
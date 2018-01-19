package com.clouway.app.core

import spark.Request
import spark.Response

interface SecuredRoute {
    fun handle(req: Request, resp: Response, session: Session): Any
}
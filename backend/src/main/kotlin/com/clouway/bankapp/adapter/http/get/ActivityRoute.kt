package com.clouway.bankapp.adapter.http.get

import com.clouway.bankapp.core.SessionRepository
import com.clouway.bankapp.core.httpresponse.HttpResponseActivity
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class ActivityRoute(
        private val sessionRepository: SessionRepository
) : Route {
    override fun handle(req: Request, resp: Response): Any =
            HttpResponseActivity(sessionRepository.getSessionsCount(LocalDateTime.now()))
}
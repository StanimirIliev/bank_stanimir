package com.clouway.app.adapter.http.get

import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.UserRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class UsersRoute(private val userRepository: UserRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val username = userRepository.getUsername(session.userId)
        return if (username == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            "{\"message\":\"Cannot get username. Invalid userId.\"}"
        } else {
            "{\"username\":\"$username\"}"
        }
    }
}
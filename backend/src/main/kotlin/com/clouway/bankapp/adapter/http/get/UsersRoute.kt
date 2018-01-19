package com.clouway.bankapp.adapter.http.get

import com.clouway.bankapp.core.httpresponse.HttpResponseMessage
import com.clouway.bankapp.core.SecuredRoute
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.UserRepository
import com.clouway.bankapp.core.httpresponse.HttpResponseUsername
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

class UsersRoute(private val userRepository: UserRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val username = userRepository.getUsername(session.userId)
        return if (username == null) {
            resp.status(HttpStatus.BAD_REQUEST_400)
            HttpResponseMessage("Cannot get username. Invalid userId.")
        } else {
            HttpResponseUsername(username)
        }
    }
}
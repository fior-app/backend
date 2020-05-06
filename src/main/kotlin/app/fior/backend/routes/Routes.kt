@file:Suppress("DuplicatedCode")

package app.fior.backend.routes

import app.fior.backend.handlers.*
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router

val routerBeans = beans {
    bean {
        router {
            "/auth".nest {
                val handler = AuthHandler(ref(), ref())
                POST("/login") { handler.login(it) }
                POST("/signup") { handler.signup(it) }
            }
            "/users".nest {
                val handler = UsersHandler(ref())
                GET("/me") { handler.me(it) }
            }

            val handler = IndexHandler()
            GET("/") { handler.get() }
        }
    }
}

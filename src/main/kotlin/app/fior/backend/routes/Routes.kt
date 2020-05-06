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
                POST("/forgotPassword") { handler.forgotPassword(it) }
                GET("/resetPassword/{token}") { handler.checkResetPassword(it) }
                POST("/resetPassword/{token}") { handler.resetPassword(it) }
            }
            "/users".nest {
                val handler = UsersHandler(ref())

                "/me".nest {
                    POST("/sendEmailConfirmation") { handler.sendEmailConfirmation(it) }
                    POST("/confirmEmail/{token}") { handler.confirmEmail(it) }
                    GET("/") { handler.me(it) }
                }
            }

            val handler = IndexHandler()
            GET("/") { handler.get() }
        }
    }
}

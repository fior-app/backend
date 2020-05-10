package app.fior.backend.routes

import app.fior.backend.handlers.AuthHandler
import app.fior.backend.handlers.IndexHandler
import app.fior.backend.handlers.UsersHandler
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router

val routerBeans = beans {
    bean {
        router {
            "/auth".nest {
                val handler = AuthHandler(ref(), ref(), ref(), ref(), ref())

                POST("/signin") { handler.signin(it) }
                POST("/signup") { handler.signup(it) }
                POST("/google") { handler.googleSignIn(it) }
                POST("/forgotPassword") { handler.forgotPassword(it) }
                GET("/checkResetPassword") { handler.checkResetPassword(it) }
                POST("/resetPassword") { handler.resetPassword(it) }
            }

            "/users".nest {
                val handler = UsersHandler(ref(), ref(), ref(), ref())

                "/me".nest {
                    POST("/sendEmailConfirmation") { handler.sendEmailConfirmation(it) }
                    POST("/confirmEmail") { handler.confirmEmail(it) }
                    POST("/changePassword") { handler.changePassword(it) }
                    GET("/") { handler.getMe(it) }
                    PUT("/") { handler.updateMe(it) }
                }
            }

            val handler = IndexHandler()
            GET("/") { handler.get() }
        }
    }
}

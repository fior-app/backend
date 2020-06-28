package app.fior.backend.routes

import app.fior.backend.handlers.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Router(
        private val indexHandler: IndexHandler,
        private val authHandler: AuthHandler,
        private val usersHandler: UsersHandler,
        private val chatroomHandler: ChatroomHandler
) {

    @Bean
    fun routes(): RouterFunction<ServerResponse> = router {
        "/auth".nest {
            POST("/signup") { authHandler.signUp(it) }

            "/signin".nest {
                POST("/email") { authHandler.signInEmail(it) }
                POST("/google") { authHandler.signInGoogle(it) }
            }
            POST("/forgotPassword") { authHandler.forgotPassword(it) }
            GET("/resetPassword") { authHandler.checkResetPassword(it) }
            POST("/resetPassword") { authHandler.resetPassword(it) }
        }

        "/users".nest {
            "/me".nest {
                POST("/sendEmailConfirmation") { usersHandler.sendEmailConfirmation(it) }
                POST("/confirmEmail") { usersHandler.confirmEmail(it) }
                POST("/changePassword") { usersHandler.changePassword(it) }
                GET("/") { usersHandler.getMe(it) }
                PUT("/") { usersHandler.updateMe(it) }
            }
        }

        GET("/chatrooms") { chatroomHandler.getPrivateChatRoom(it) }

        GET("/") { indexHandler.get() }
    }
}
package app.fior.backend.routes

import app.fior.backend.handlers.AuthHandler
import app.fior.backend.handlers.IndexHandler
import app.fior.backend.handlers.QuestionHandler
import app.fior.backend.handlers.UsersHandler
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
        private val questionHandler: QuestionHandler
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

        "/questions".nest {
            GET("/") { questionHandler.getQuestions(it) }
            GET("/{id}") { questionHandler.getQuestion(it) }
            POST("/") { questionHandler.createQuestion(it) }
            PATCH("/{id}") { questionHandler.updateQuestion(it) }
            DELETE("/{id}") { questionHandler.deleteQuestion(it) }
        }

        GET("/") { indexHandler.get() }
    }
}
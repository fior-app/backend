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
        private val chatroomHandler: ChatroomHandler,
        private val questionHandler: QuestionHandler,
        private val skillHandler: SkillHandler
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

        "/chatrooms".nest {
            GET("/private") { chatroomHandler.getPrivateChatRoom(it) }
            POST("/send") { chatroomHandler.sendMessage(it) }
        }

        "/questions".nest {
            GET("/") { questionHandler.getQuestions(it) }
            GET("/{questionId}") { questionHandler.getQuestion(it) }
            POST("/") { questionHandler.createQuestion(it) }
            PATCH("/{questionId}") { questionHandler.updateQuestion(it) }
            DELETE("/{questionId}") { questionHandler.deleteQuestion(it) }
            GET("/{questionId}/answers") { questionHandler.getAnswers(it) }
            POST("/{questionId}/answers") { questionHandler.createAnswer(it) }
            PATCH("/{questionId}/answers/{answerId}") { questionHandler.updateAnswer(it) }
            DELETE("/{questionId}/answers/{answerId}") { questionHandler.deleteAnswer(it) }
            POST("/{questionId}/answers/{answerId}/correct") { questionHandler.setCorrectAnswer(it) }
        }

        "/skills".nest {
            GET("/") { skillHandler.getSkills(it) }
            GET("/search") { skillHandler.searchSkills(it) }
            POST("/") { skillHandler.createSkill(it) }
            DELETE("/{id}") { skillHandler.deleteSkill(it) }
        }

        GET("/") { indexHandler.get() }
    }
}
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
        private val skillHandler: SkillHandler,
        private val userSkillHandler: UserSkillHandler,
        private val commentHandler: CommentHandler,
        private val groupHandler: GroupHandler,
        private val postHandler: PostHandler
) {

    @Bean
    fun routes(): RouterFunction<ServerResponse> = router {
        "/auth".nest {
            POST("/signup") { authHandler.signUp(it) }

            "/signin".nest {
                POST("/email") { authHandler.signInEmail(it) }
                POST("/google") { authHandler.signInGoogle(it) }
                POST("/linkedin") { authHandler.signInLinkedIn(it) }
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
                POST("/uploadProfilePicture") { usersHandler.uploadProfilePicture(it) }
            }
        }

        "/chatrooms".nest {
            POST("/private") { chatroomHandler.getPrivateChatRoom(it) }
            POST("/{roomId}/send") { chatroomHandler.sendMessage(it) }
            GET("/private/{roomId}/messages") { chatroomHandler.getPrivateMessages(it) }
            GET("/groups/{groupId}/messages") { chatroomHandler.getGroupMessages(it) }
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

        "/posts".nest {
            GET("/") { postHandler.getPosts(it) }
            GET("/{postId}") { postHandler.getPost(it) }
            POST("/") { postHandler.createPost(it) }
            PATCH("/{postId}") { postHandler.updatePost(it) }
            DELETE("/{postId}") { postHandler.deletePost(it) }
        }

        "/comments".nest {
            POST("/") { commentHandler.createComment(it) }
            PATCH("/{commentId}") { commentHandler.updateComment(it) }
            DELETE("/{commentId}") { commentHandler.deleteComment(it) }
        }

        "/skills".nest {
            GET("/") { skillHandler.getSkills(it) }
            GET("/search") { skillHandler.searchSkills(it) }
            POST("/") { skillHandler.createSkill(it) }
            DELETE("/{skillId}") { skillHandler.deleteSkill(it) }
            GET("/{skillId}/questions") { skillHandler.getSkillQuestionSet(it) }
            POST("/{skillId}/questions") { skillHandler.createSkillQuestion(it) }
            PATCH("/{skillId}/questions/{skillQuestionId}") { skillHandler.updateSkillQuestion(it) }
            DELETE("/{skillId}/questions/{skillQuestionId}") { skillHandler.deleteSkillQuestion(it) }
        }

        "/userskills".nest {
            GET("/") { userSkillHandler.getUserSkills(it) }
            POST("/") { userSkillHandler.addSkill(it) }
            DELETE("/{userskillId}") { userSkillHandler.removeSkill(it) }
            POST("/{userskillId}/verify") { userSkillHandler.verifyUserSkill(it) }
        }

        "groups".nest {
            POST("/") { groupHandler.createGroup(it) }
            GET("/me") { groupHandler.groupsMe(it) }
            GET("/{groupId}") { groupHandler.getGroup(it) }
            GET("/{groupId}/members") { groupHandler.getGroupMembers(it) }
            GET("/me/all") { groupHandler.groupsMeAll(it) }
            GET("/me/requests") { groupHandler.groupsMeRequests(it) }
            "/{groupId}/member".nest {
                POST("/") { groupHandler.requestMemberToGroup(it) }
                POST("/leave") { groupHandler.leaveGroup(it) }
                POST("/state") { groupHandler.changeGroupState(it) }
            }
        }

        GET("/") { indexHandler.get() }
    }
}
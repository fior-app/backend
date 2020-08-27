package app.fior.backend.handlers

import app.fior.backend.data.SkillQuestionRepository
import app.fior.backend.data.SkillRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.data.UserSkillRepository
import app.fior.backend.dto.SkillQuestionAnswersRequest
import app.fior.backend.dto.UserSkillRequest
import app.fior.backend.extensions.*
import app.fior.backend.model.Skill
import app.fior.backend.model.UserSkill
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty


@Component
class UserSkillHandler(
        @Value("\${fior.business.questions-to-verify}") private val questionsToVerify: Int,
        private val skillRepository: SkillRepository,
        private val userRepository: UserRepository,
        private val userSkillRepository: UserSkillRepository,
        private val skillQuestionRepository: SkillQuestionRepository
) {

    fun getUserSkills(request: ServerRequest) = request.principalUser(userRepository).flatMap { user ->
        ServerResponse.ok().body(
                userSkillRepository.findAllByUserId(user.id!!)
                        .skip(request.queryParam("skip").orElse("0").toLong())
                        .take(request.queryParam("limit").orElse("25").toLong()),
                UserSkill::class.java
        )
    }

    fun addSkill(request: ServerRequest) = request.bodyToMono(UserSkillRequest::class.java)
            .flatMap { skillRequest ->
                Mono.zip(
                        request.principalUser(userRepository),
                        skillRepository.findById(skillRequest.skillId)
                ).flatMap { (user, skill) ->
                    userSkillRepository.save(UserSkill(user, skill)).flatMap {
                        "User skill added successfully".toSuccessServerResponse()
                    }
                }.switchIfEmpty {
                    "User skill not found".toNotFoundServerResponse()
                }
            }

    fun removeSkill(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            userSkillRepository.findById(request.pathVariable("userskillId"))
    ).flatMap { (user, userSkill) ->
        if (userSkill.userId != user.id) {
            return@flatMap "Unauthorized".toUnauthorizedServerResponse()
        }

        userSkillRepository.delete(userSkill).thenReturn(true).flatMap {
            "User skill removed successfully".toSuccessServerResponse()
        }
    }.switchIfEmpty {
        "User skill not found".toNotFoundServerResponse()
    }

    fun verifyUserSkill(request: ServerRequest) = userSkillRepository.findById(request.pathVariable("userskillId")).flatMap { userSkill ->
        val answerSetInvalidKey = "invalid_answers"

        request.bodyToMono(SkillQuestionAnswersRequest::class.java).flatMap request@{ skillAnswersRequest ->
            if (skillAnswersRequest.answers.size != questionsToVerify) {
                println("Question size error")
                return@request "Answer set size invalid".toBadRequestServerResponse()
            }

            skillQuestionRepository.findAllByIdIn(skillAnswersRequest.answers.map { it.questionId }).collectList().map { questionList ->
                if (questionList.size != questionsToVerify) {
                    throw Exception(answerSetInvalidKey)
                }

                questionList.all {
                    val userAnswer = skillAnswersRequest.answers.find { answer -> answer.questionId == it.id }?.answer
                    println(userAnswer)
                    println(it.answer)
                    it.answer == userAnswer && it.skillId == userSkill.skill.id
                }
            }.flatMap answerCheck@{
                if (!it) {
                    throw Exception(answerSetInvalidKey)
                }

                userSkillRepository.save(userSkill.copy(isVerified = true)).flatMap {
                    "User skill verified successfully".toSuccessServerResponse()
                }
            }.onErrorResume {
                if (it.message == answerSetInvalidKey) {
                    println("Set invalid")
                    "Invalid answer set".toBadRequestServerResponse()
                }

                println("Unknown error")
                "Unknown error".toBadRequestServerResponse()
            }
        }
    }.switchIfEmpty {
        "User skill not found".toNotFoundServerResponse()
    }


}

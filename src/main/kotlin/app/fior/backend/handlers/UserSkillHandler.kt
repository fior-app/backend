package app.fior.backend.handlers

import app.fior.backend.data.SkillRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.data.UserSkillRepository
import app.fior.backend.dto.UserSkillRequest
import app.fior.backend.extensions.*
import app.fior.backend.model.Skill
import app.fior.backend.model.UserSkill
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty


@Component
class UserSkillHandler(
        private val skillRepository: SkillRepository,
        private val userRepository: UserRepository,
        private val userSkillRepository: UserSkillRepository
) {

    fun getUserSkills(request: ServerRequest) = request.principalUser(userRepository).flatMap { user ->
        ServerResponse.ok().body(
                userSkillRepository.findAllByUserId(user.id!!)
                        .skip(request.queryParam("skip").orElse("0").toLong())
                        .take(request.queryParam("limit").orElse("25").toLong()),
                Skill::class.java
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

        userSkillRepository.delete(userSkill).flatMap {
            "User skill removed successfully".toSuccessServerResponse()
        }
    }.switchIfEmpty {
        "User skill not found".toNotFoundServerResponse()
    }

}
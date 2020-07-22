package app.fior.backend.handlers

import app.fior.backend.data.SkillQuestionRepository
import app.fior.backend.data.SkillRepository
import app.fior.backend.dto.*
import app.fior.backend.extensions.*
import app.fior.backend.model.Skill
import app.fior.backend.model.SkillQuestion
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono


@Component
class SkillHandler(
        private val skillRepository: SkillRepository,
        private val skillQuestionRepository: SkillQuestionRepository
) {

    fun getSkills(request: ServerRequest) = ServerResponse.ok().body(
            skillRepository.findAll()
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            Skill::class.java
    )

    fun getSkill(request: ServerRequest) = skillRepository.findById(request.pathVariable("skillId")).flatMap {
        ServerResponse.ok().bodyValue(it)
    }.switchIfEmpty {
        return@switchIfEmpty "Skill not found".toNotFoundServerResponse()
    }

    fun searchSkills(request: ServerRequest) = request.queryParam("q").toMono()
            .flatMap { query ->
                if (!query.isPresent)
                    return@flatMap "Search query not found".toBadRequestServerResponse()

                ServerResponse.ok().body(
                        skillRepository.findAllByNameLike(query.get())
                                .take(request.queryParam("limit").orElse("25").toLong()),
                        Skill::class.java
                )
            }

    fun createSkill(request: ServerRequest) = request.bodyToMono(SkillCreateRequest::class.java)
            .flatMap { skill ->
                skillRepository.save(Skill(skill)).flatMap {
                    "Skill created successfully".toSuccessServerResponse()
                }
            }

    fun deleteSkill(request: ServerRequest) = skillRepository.existsById(request.pathVariable("skillId"))
            .flatMap {
                if (!it)
                    return@flatMap "Skill not found".toNotFoundServerResponse()

                skillRepository.deleteById(request.pathVariable("skillId")).thenReturn(true).flatMap {
                    "Skill deleted successfully".toSuccessServerResponse()
                }
            }

    fun getSkillQuestionSet(request: ServerRequest) = skillRepository.existsById(request.pathVariable("skillId"))
            .flatMap { skillExists ->
                if (!skillExists)
                    return@flatMap "Skill not found".toNotFoundServerResponse()

                ServerResponse.ok().body(
                        skillQuestionRepository.findAllBySkillId(request.pathVariable("skillId"))
                                .collectList()
                                .map { it.shuffled().take(3) }
                                .flatMapIterable { it },
                        SkillQuestion::class.java
                )
            }

    fun getFullSkillQuestions(request: ServerRequest) = skillRepository.existsById(request.pathVariable("skillId"))
            .flatMap { skillExists ->
                if (!skillExists)
                    return@flatMap "Skill not found".toNotFoundServerResponse()

                ServerResponse.ok().body(
                        skillQuestionRepository.findAllBySkillId(request.pathVariable("skillId"))
                                .map { SkillQuestionFullResponse(it) },
                        SkillQuestionFullResponse::class.java
                )
            }

    fun createSkillQuestion(request: ServerRequest) = skillRepository.existsById(request.pathVariable("skillId"))
            .flatMap { skillExists ->
                if (!skillExists)
                    return@flatMap "Skill not found".toNotFoundServerResponse()

                request.bodyToMono(SkillQuestionCreateRequest::class.java)
                        .flatMap { skillRequest ->
                            skillQuestionRepository.save(SkillQuestion(
                                    request.pathVariable("skillId"),
                                    skillRequest
                            )).flatMap {
                                "Skill question created successfully".toSuccessServerResponse()
                            }
                        }
            }

    fun updateSkillQuestion(request: ServerRequest) = skillQuestionRepository.findById(request.pathVariable("skillQuestionId"))
            .flatMap { skillQuestion ->
                if (skillQuestion.skillId != request.pathVariable("skillId")) {
                    return@flatMap Mono.empty<ServerResponse>()
                }

                request.bodyToMono(SkillQuestionUpdateRequest::class.java).flatMap { updateRequest ->
                    skillQuestionRepository.save(skillQuestion.updated(updateRequest)).flatMap {
                        "Skill question updated successfully".toSuccessServerResponse()
                    }
                }
            }.switchIfEmpty {
                "Skill question not found".toNotFoundServerResponse()
            }

    fun deleteSkillQuestion(request: ServerRequest) = skillQuestionRepository.findById(request.pathVariable("skillQuestionId"))
            .flatMap { skillQuestion ->
                if (skillQuestion.skillId != request.pathVariable("skillId")) {
                    return@flatMap Mono.empty<ServerResponse>()
                }

                skillQuestionRepository.deleteById(skillQuestion.id!!).thenReturn(true)
                        .flatMap {
                            "Skill question deleted successfully".toSuccessServerResponse()
                        }

            }.switchIfEmpty {
                "Skill question not found".toNotFoundServerResponse()
            }

}
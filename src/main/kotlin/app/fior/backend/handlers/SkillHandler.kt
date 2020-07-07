package app.fior.backend.handlers

import app.fior.backend.data.SkillRepository
import app.fior.backend.dto.SkillCreateRequest
import app.fior.backend.extensions.toBadRequestServerResponse
import app.fior.backend.extensions.toNotFoundServerResponse
import app.fior.backend.extensions.toSuccessServerResponse
import app.fior.backend.model.Skill
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono


@Component
class SkillHandler(
        private val skillRepository: SkillRepository
) {

    fun getSkills(request: ServerRequest) = ServerResponse.ok().body(
            skillRepository.findAll()
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            Skill::class.java
    )

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

    fun deleteSkill(request: ServerRequest) = skillRepository.findById(request.pathVariable("id"))
            .flatMap { skill ->
                skillRepository.deleteById(skill.id!!).thenReturn(true).flatMap {
                    "Skill deleted successfully".toSuccessServerResponse()
                }
            }.switchIfEmpty {
                "Skill not found".toNotFoundServerResponse()
            }

}
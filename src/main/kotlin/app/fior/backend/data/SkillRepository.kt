package app.fior.backend.data

import app.fior.backend.model.Skill
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface SkillRepository : ReactiveMongoRepository<Skill, String> {

    fun findAllByNameLike(name: String): Flux<Skill>

}
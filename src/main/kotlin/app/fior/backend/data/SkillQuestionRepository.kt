package app.fior.backend.data

import app.fior.backend.model.SkillQuestion
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface SkillQuestionRepository : ReactiveMongoRepository<SkillQuestion, String> {

    fun findAllBySkillId(skillId: String): Flux<SkillQuestion>

}
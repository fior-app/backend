package app.fior.backend.data

import app.fior.backend.model.UserSkill
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserSkillRepository : ReactiveMongoRepository<UserSkill, String> {

    fun findAllByUserId(userId: String): Flux<UserSkill>

    fun findByUserIdAndSkillId(userId: String, skillId: String): Mono<UserSkill>

}
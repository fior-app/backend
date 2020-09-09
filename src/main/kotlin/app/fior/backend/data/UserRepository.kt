package app.fior.backend.data

import app.fior.backend.model.Role
import app.fior.backend.model.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<User, String> {

    fun findByEmail(email: String): Mono<User>

    fun findAllByRolesContains(role: Role): Flux<User>

    fun findAllByisMentorAndNameLike(isMentor: Boolean, name: String): Flux<User>

}

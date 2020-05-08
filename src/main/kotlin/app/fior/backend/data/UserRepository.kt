package app.fior.backend.data

import app.fior.backend.model.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<User, String> {

    fun findByEmail(email: String): Mono<User>

}
package app.fior.backend.extensions

import app.fior.backend.data.UserRepository
import app.fior.backend.model.User
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono

fun ServerRequest.principalUser(userRepository: UserRepository): Mono<User> {
    return this.principal().flatMap {
        userRepository.findByEmail(it.name)
    }
}
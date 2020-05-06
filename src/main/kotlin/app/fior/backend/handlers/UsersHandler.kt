package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ApiResponse
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class UsersHandler(
        private val userRepository: UserRepository
) {

    fun me(request: ServerRequest) = request.principal().flatMap { principal ->
        Mono.justOrEmpty(userRepository.findByEmail(principal.name))
                .flatMap {
                    ServerResponse.ok().bodyValue(it)
                }.switchIfEmpty {
                    ServerResponse.noContent().build()
                }
    }

}
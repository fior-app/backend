package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ApiResponse
import app.fior.backend.dto.LoginRequest
import app.fior.backend.dto.LoginResponse
import app.fior.backend.dto.SignupRequest
import app.fior.backend.model.User
import app.fior.backend.security.TokenProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class AuthHandler(
        private val userRepository: UserRepository,
        private val passwordEncoder: BCryptPasswordEncoder
) {

    fun login(request: ServerRequest) = request.bodyToMono(LoginRequest::class.java).flatMap { login ->
        Mono.justOrEmpty(userRepository.findByEmail(login.username))
                .flatMap { user ->
                    if (passwordEncoder.matches(login.password, user.password!!)) {
                        ServerResponse.ok().bodyValue(LoginResponse(TokenProvider.generateToken(user)))
                    } else {
                        ServerResponse.badRequest().bodyValue(ApiResponse("Invalid credentials"))
                    }
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ApiResponse("User does not exist"))
                }
    }

    fun signup(request: ServerRequest) = request.bodyToMono(SignupRequest::class.java).map { user ->
        user.password = passwordEncoder.encode(user.password)
        user
    }.flatMap { user ->
        Mono.justOrEmpty(userRepository.findByEmail(user.email))
                .flatMap { ServerResponse.badRequest().bodyValue(ApiResponse("User already exist")) }
                .switchIfEmpty {
                    Mono.just(userRepository.save(User(user))).flatMap {
                        ServerResponse.ok().bodyValue(ApiResponse("User created successfully"))
                    }
                }
    }
}
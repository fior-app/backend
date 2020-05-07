package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.*
import app.fior.backend.model.User
import app.fior.backend.security.TokenProvider
import app.fior.backend.services.EmailService
import io.jsonwebtoken.Claims
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class AuthHandler(
        private val userRepository: UserRepository,
        private val passwordEncoder: BCryptPasswordEncoder
) {

    fun signin(request: ServerRequest) = request.bodyToMono(LoginRequest::class.java).flatMap { login ->
        Mono.justOrEmpty(userRepository.findByEmail(login.email))
                .flatMap { user ->
                    if (passwordEncoder.matches(login.password, user.password!!)) {
                        ServerResponse.ok().bodyValue(LoginResponse(TokenProvider.generateAuthToken(user)))
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

    fun forgotPassword(request: ServerRequest) = request.bodyToMono(ForgotPasswordRequest::class.java).flatMap { req ->
        Mono.justOrEmpty(userRepository.findByEmail(req.email))
                .flatMap {
                    val resetToken = TokenProvider.generateResetToken(it)

                    EmailService.sendForgotPassword(it.email!!, resetToken).flatMap {
                        ServerResponse.ok().bodyValue(ApiResponse("Password reset email sent"))
                    }
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                }
    }

    fun checkResetPassword(request: ServerRequest) = Mono.just(request.pathVariable("token")).flatMap { token ->
        val email = try {
            TokenProvider.getUsernameFromToken(token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = TokenProvider.getAllClaimsFromToken(token)
        val isReset = claims[TokenProvider.RESET_KEY] as Boolean

        if (email != null && !TokenProvider.isTokenExpired(token) && isReset) {
            Mono.justOrEmpty(userRepository.findByEmail(email))
                    .flatMap {
                        ServerResponse.ok().bodyValue(it)
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                    }
        } else ServerResponse.badRequest().bodyValue(ErrorResponse("Reset token is not valid"))
    }

    fun resetPassword(request: ServerRequest) = request.bodyToMono(ResetPasswordRequest::class.java).flatMap { req ->
        val token = request.pathVariable("token")

        val email = try {
            TokenProvider.getUsernameFromToken(token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = TokenProvider.getAllClaimsFromToken(token)
        val isReset = claims[TokenProvider.RESET_KEY] as Boolean

        if (email != null && !TokenProvider.isTokenExpired(token) && isReset) {
            Mono.justOrEmpty(userRepository.findByEmail(email))
                    .flatMap {
                        it.password = passwordEncoder.encode(req.password)

                        Mono.just(userRepository.save(it)).flatMap {
                            ServerResponse.ok().bodyValue(ApiResponse("Password reset successfully"))
                        }
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                    }
        } else ServerResponse.badRequest().bodyValue(ErrorResponse("Reset token is not valid"))
    }
}
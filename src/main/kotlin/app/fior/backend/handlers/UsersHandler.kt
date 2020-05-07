package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ApiResponse
import app.fior.backend.dto.ErrorResponse
import app.fior.backend.dto.UpdateUserRequest
import app.fior.backend.security.TokenProvider
import app.fior.backend.services.EmailService
import io.jsonwebtoken.Claims
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class UsersHandler(
        private val userRepository: UserRepository
) {

    fun getMe(request: ServerRequest) = request.principal().flatMap { principal ->
        Mono.justOrEmpty(userRepository.findByEmail(principal.name))
                .flatMap {
                    ServerResponse.ok().bodyValue(it)
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                }
    }

    fun updateMe(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(UpdateUserRequest::class.java).flatMap { updateUserRequest ->
            Mono.justOrEmpty(userRepository.findByEmail(principal.name))
                    .flatMap userUpdate@ { user ->
                        updateUserRequest.name?.let {
                            user.name = updateUserRequest.name
                        }

                        updateUserRequest.email?.let {
                            if (updateUserRequest.email != user.email && userRepository.findByEmail(updateUserRequest.email).isEmpty) {
                                user.email = updateUserRequest.email
                                user.emailValid = false
                            } else return@userUpdate ServerResponse.badRequest().bodyValue(ApiResponse("User with given email already exists"))
                        }

                        Mono.just(userRepository.save(user)).flatMap {
                            ServerResponse.ok().bodyValue(ApiResponse("User updated successfully"))
                        }
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                    }
        }
    }

    fun sendEmailConfirmation(request: ServerRequest) = request.principal().flatMap { principal ->
        Mono.justOrEmpty(userRepository.findByEmail(principal.name))
                .flatMap {
                    EmailService.sendEmailConfirmation(it.email!!, TokenProvider.generateConfirmToken(it)).flatMap {
                        ServerResponse.badRequest().bodyValue(ApiResponse("Email confirmation request sent"))
                    }
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                }
    }

    fun confirmEmail(request: ServerRequest) = Mono.just(request.pathVariable("token")).flatMap { token ->
        val email = try {
            TokenProvider.getUsernameFromToken(token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = TokenProvider.getAllClaimsFromToken(token)
        val isConfirm = claims[TokenProvider.CONFIRM_KEY] as Boolean

        if (email != null && !TokenProvider.isTokenExpired(token) && isConfirm) {
            Mono.justOrEmpty(userRepository.findByEmail(email))
                    .flatMap {
                        it.emailValid = true
                        Mono.just(userRepository.save(it)).flatMap {
                            ServerResponse.ok().bodyValue(ApiResponse("Email confirmed successfully"))
                        }
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                    }
        } else ServerResponse.badRequest().bodyValue(ErrorResponse("Reset token is not valid"))
    }

}
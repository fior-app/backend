package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ApiResponse
import app.fior.backend.dto.ChangePasswordRequest
import app.fior.backend.dto.ErrorResponse
import app.fior.backend.dto.UpdateUserRequest
import app.fior.backend.services.TokenService
import app.fior.backend.services.EmailService
import io.jsonwebtoken.Claims
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class UsersHandler(
        private val userRepository: UserRepository,
        private val emailService: EmailService,
        private val tokenService: TokenService,
        private val passwordEncoder: BCryptPasswordEncoder
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
                    .flatMap getUser@{ user ->
                        updateUserRequest.name?.let {
                            user.name = updateUserRequest.name
                        }

                        updateUserRequest.email?.let {
                            if (updateUserRequest.email != user.email && userRepository.findByEmail(updateUserRequest.email).isEmpty) {
                                user.email = updateUserRequest.email
                                user.emailValid = false
                            } else return@getUser ServerResponse.badRequest().bodyValue(ApiResponse("User with given email already exists"))
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
                    emailService.sendEmailConfirmation(it.email!!, tokenService.generateConfirmToken(it)).flatMap {
                        ServerResponse.badRequest().bodyValue(ApiResponse("Email confirmation request sent"))
                    }
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
                }
    }

    fun confirmEmail(request: ServerRequest) = Mono.just(request.pathVariable("token")).flatMap { token ->
        val email = try {
            tokenService.getUsernameFromToken(token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = tokenService.getAllClaimsFromToken(token)
        val isConfirm = claims[TokenService.CONFIRM_KEY] as Boolean

        if (email != null && !tokenService.isTokenExpired(token) && isConfirm) {
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

    fun changePassword(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(ChangePasswordRequest::class.java).flatMap { changePasswordRequest ->
            Mono.justOrEmpty(userRepository.findByEmail(principal.name)).flatMap getUser@{ user ->
                if (!user.hasPassword) {
                    return@getUser ServerResponse.badRequest().bodyValue(ApiResponse("User doesn't has a password"))
                }

                if (!passwordEncoder.matches(changePasswordRequest.oldPassword, user.password!!)) {
                    return@getUser ServerResponse.badRequest().bodyValue(ApiResponse("Old password didn't match"))
                }

                user.password = changePasswordRequest.newPassword
                Mono.just(userRepository.save(user)).flatMap {
                    ServerResponse.ok().bodyValue(ApiResponse("Password changed successfully"))
                }
            }.switchIfEmpty {
                ServerResponse.badRequest().bodyValue(ApiResponse("User not found"))
            }
        }
    }

}
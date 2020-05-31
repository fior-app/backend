package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.*
import app.fior.backend.model.User
import app.fior.backend.services.EmailService
import app.fior.backend.services.GoogleAuthService
import app.fior.backend.services.TokenService
import io.jsonwebtoken.Claims
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class AuthHandler(
        private val userRepository: UserRepository,
        private val passwordEncoder: BCryptPasswordEncoder,
        private val emailService: EmailService,
        private val tokenService: TokenService,
        private val googleAuthService: GoogleAuthService
) {

    fun signUp(signUpRequest: ServerRequest) = signUpRequest.bodyToMono(SignUpRequest::class.java).map { request ->
        request.password = passwordEncoder.encode(request.password)
        request
    }.flatMap { request ->
        userRepository.findByEmail(request.email)
                .flatMap { ServerResponse.badRequest().bodyValue(ErrorResponse("User already exist")) }
                .switchIfEmpty {
                    userRepository.save(User(request)).flatMap {
                        ServerResponse.ok().bodyValue(SuccessResponse("User created successfully"))
                    }
                }
    }

    fun signInEmail(request: ServerRequest) = request.bodyToMono(SignInEmailRequest::class.java).flatMap { login ->
        userRepository.findByEmail(login.email)
                .flatMap { user ->
                    if (passwordEncoder.matches(login.password, user.password)) {
                        ServerResponse.ok().bodyValue(SignInResponse(tokenService.generateAuthToken(user)))
                    } else {
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ErrorResponse("Invalid credentials"))
                    }
                }.switchIfEmpty {
                    ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ErrorResponse("User does not exist"))
                }
    }

    fun signInGoogle(request: ServerRequest) = request.bodyToMono(SignInGoogleRequest::class.java).flatMap { googleSignInRequest ->
        googleAuthService.verifyIdToken(googleSignInRequest.idToken)
    }.flatMap { payload ->
        userRepository.findByEmail(payload.email).flatMap {
            ServerResponse.ok().bodyValue(SignInResponse(tokenService.generateAuthToken(it)))
        }.switchIfEmpty {
            userRepository.save(User(payload)).flatMap {
                ServerResponse.ok().bodyValue(SignInResponse(tokenService.generateAuthToken(it)))
            }
        }
    }.switchIfEmpty {
        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ErrorResponse("Invalid id token"))
    }

    fun forgotPassword(request: ServerRequest) = request.bodyToMono(ForgotPasswordRequest::class.java).flatMap { req ->
        userRepository.findByEmail(req.email)
                .flatMap {
                    val resetToken = tokenService.generateResetToken(it)

                    emailService.sendForgotPassword(it.email, resetToken).flatMap {
                        ServerResponse.ok().bodyValue(SuccessResponse("Password reset email sent"))
                    }
                }.switchIfEmpty {
                    ServerResponse.badRequest().bodyValue(ErrorResponse("User not found"))
                }
    }

    fun checkResetPassword(request: ServerRequest) = Mono.justOrEmpty(request.queryParam("token")).flatMap { token ->
        val email = try {
            tokenService.getUsernameFromToken(token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = tokenService.getAllClaimsFromToken(token)
        val isReset = claims[TokenService.RESET_KEY] as Boolean

        if (email != null && !tokenService.isTokenExpired(token) && isReset) {
            userRepository.findByEmail(email)
                    .flatMap {
                        ServerResponse.ok().bodyValue(it)
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ErrorResponse("User not found"))
                    }
        } else ServerResponse.badRequest().bodyValue(ErrorResponse("Reset token is not valid"))
    }.switchIfEmpty {
        ServerResponse.badRequest().bodyValue(ErrorResponse("Token query parameter is not found"))
    }

    fun resetPassword(request: ServerRequest) = request.bodyToMono(ResetPasswordRequest::class.java).flatMap { resetPasswordRequest ->
        val email = try {
            tokenService.getUsernameFromToken(resetPasswordRequest.token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = tokenService.getAllClaimsFromToken(resetPasswordRequest.token)
        val isReset = claims[TokenService.RESET_KEY] as Boolean

        if (email != null && !tokenService.isTokenExpired(resetPasswordRequest.token) && isReset) {
            userRepository.findByEmail(email)
                    .flatMap {
                        val updatedUser = it.copy(password = passwordEncoder.encode(resetPasswordRequest.password))

                        userRepository.save(updatedUser).flatMap {
                            ServerResponse.ok().bodyValue(SuccessResponse("Password reset successfully"))
                        }
                    }.switchIfEmpty {
                        ServerResponse.badRequest().bodyValue(ErrorResponse("User not found"))
                    }
        } else ServerResponse.badRequest().bodyValue(ErrorResponse("Reset token is not valid"))
    }
}
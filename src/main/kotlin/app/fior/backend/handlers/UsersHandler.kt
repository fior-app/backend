package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.*
import app.fior.backend.extensions.toBadRequestServerResponse
import app.fior.backend.extensions.toSuccessServerResponse
import app.fior.backend.extensions.toUnauthorizedServerResponse
import app.fior.backend.services.BlobService
import app.fior.backend.services.EmailService
import app.fior.backend.services.TokenService
import io.jsonwebtoken.Claims
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Component
class UsersHandler(
        private val userRepository: UserRepository,
        private val emailService: EmailService,
        private val tokenService: TokenService,
        private val passwordEncoder: BCryptPasswordEncoder,
        private val blobService: BlobService
) {

    fun getMe(request: ServerRequest) = request.principal().flatMap { principal ->
        userRepository.findByEmail(principal.name)
                .flatMap {
                    ServerResponse.ok().bodyValue(it)
                }.switchIfEmpty {
                    ServerResponse.status(401).bodyValue(SuccessResponse("User not found"))
                }
    }

    fun updateMe(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(UpdateUserRequest::class.java).flatMap { updateUserRequest ->
            userRepository.findByEmail(principal.name)
                    .flatMap getUser@{ user ->
                        val canUpdateEmail = if (updateUserRequest.email != null && updateUserRequest.email != user.email) {
                            userRepository.findByEmail(updateUserRequest.email).block() != null
                        } else return@getUser "User with given email already exists".toBadRequestServerResponse()

                        val updatedUser = user.copy(
                                name = updateUserRequest.name ?: user.name,
                                email = if (canUpdateEmail) updateUserRequest.email else user.email,
                                emailValid = if (canUpdateEmail) false else user.emailValid
                        )

                        userRepository.save(updatedUser).flatMap {
                            "User updated successfully".toSuccessServerResponse()
                        }
                    }.switchIfEmpty {
                        "User not found".toUnauthorizedServerResponse()
                    }
        }
    }

    fun sendEmailConfirmation(request: ServerRequest) = request.principal().flatMap { principal ->
        userRepository.findByEmail(principal.name)
                .flatMap {
                    emailService.sendEmailConfirmation(it.email, tokenService.generateConfirmToken(it)).flatMap {
                        "Email confirmation request sent".toSuccessServerResponse()
                    }
                }.switchIfEmpty {
                    "User not found".toUnauthorizedServerResponse()
                }
    }

    fun confirmEmail(request: ServerRequest) = request.bodyToMono(ConfirmEmailRequest::class.java).flatMap { confirmEmailRequest ->
        val email = try {
            tokenService.getUsernameFromToken(confirmEmailRequest.token)
        } catch (e: Exception) {
            null
        }

        val claims: Claims = tokenService.getAllClaimsFromToken(confirmEmailRequest.token)
        val isConfirm = claims[TokenService.CONFIRM_KEY] as Boolean

        if (email != null && !tokenService.isTokenExpired(confirmEmailRequest.token) && isConfirm) {
            userRepository.findByEmail(email)
                    .flatMap {
                        val updatedUser = it.copy(emailValid = true)

                        userRepository.save(updatedUser).flatMap {
                            "Email confirmed successfully".toSuccessServerResponse()
                        }
                    }.switchIfEmpty {
                        "User not found".toUnauthorizedServerResponse()
                    }
        } else "Reset token is not valid".toBadRequestServerResponse()
    }

    fun changePassword(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(ChangePasswordRequest::class.java).flatMap { changePasswordRequest ->
            userRepository.findByEmail(principal.name).flatMap getUser@{ user ->
                if (!user.hasPassword) {
                    return@getUser "User doesn't has a password".toBadRequestServerResponse()
                }

                if (!passwordEncoder.matches(changePasswordRequest.oldPassword, user.password)) {
                    return@getUser "Old password didn't match".toBadRequestServerResponse()
                }

                val updatedUser = user.copy(password = changePasswordRequest.newPassword)

                userRepository.save(updatedUser).flatMap {
                    "Password changed successfully".toSuccessServerResponse()
                }
            }.switchIfEmpty {
                "User not found".toUnauthorizedServerResponse()
            }
        }
    }

    fun uploadProfilePicture(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(FileUploadDto::class.java).flatMap { fileUpload ->
            userRepository.findByEmail(principal.name).flatMap { user ->
                val data = Base64Utils.decodeFromString(fileUpload.data)
                val filename = "${UUID.randomUUID()}.${fileUpload.ext}"

                Mono.zip(
                        blobService.deleteBlob((user.profilePicture ?: "").split("/").last())
                                .onErrorReturn(false),
                        blobService.uploadBlob(filename, data).flatMap {
                            userRepository.save(user.copy(profilePicture = it))
                        }
                ).flatMap {
                    "Profile picture uploaded successfully".toSuccessServerResponse()
                }
            }.switchIfEmpty {
                "User not found".toUnauthorizedServerResponse()
            }
        }
    }

}
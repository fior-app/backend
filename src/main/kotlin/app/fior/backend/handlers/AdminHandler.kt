package app.fior.backend.handlers

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.EmailRequest
import app.fior.backend.extensions.toBadRequestServerResponse
import app.fior.backend.extensions.toNotFoundServerResponse
import app.fior.backend.extensions.toSuccessServerResponse
import app.fior.backend.model.Role
import app.fior.backend.model.User
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class AdminHandler(
        private val userRepository: UserRepository
) {
    fun getAdmins(request: ServerRequest) = ServerResponse.ok().body(
            userRepository.findAllByRolesContains(Role.ADMIN)
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            User::class.java
    )

    fun createAdmin(request: ServerRequest) = request.bodyToMono(EmailRequest::class.java)
            .flatMap { (email) ->
                userRepository.findByEmail(email).flatMap user@{
                    val roles = it.roles.toMutableSet()
                    if (!roles.add(Role.ADMIN)) {
                        return@user "User is already an admin".toBadRequestServerResponse()
                    }

                    userRepository.save(it.copy(roles = roles.toList())).flatMap {
                        "Admin created successfully".toSuccessServerResponse()
                    }
                }.switchIfEmpty {
                    "User not found".toNotFoundServerResponse()
                }
            }

    fun deleteAdmin(request: ServerRequest) = userRepository.findById(request.pathVariable("userId"))
            .flatMap {
                val roles = it.roles.toMutableSet()
                if (!roles.remove(Role.ADMIN)) {
                    return@flatMap "User is not as admin".toBadRequestServerResponse()
                }

                userRepository.save(it.copy(roles = roles.toList())).flatMap {
                    "Admin deleted successfully".toSuccessServerResponse()
                }
            }.switchIfEmpty {
                "User not found".toNotFoundServerResponse()
            }

}
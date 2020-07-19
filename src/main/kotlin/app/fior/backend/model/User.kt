package app.fior.backend.model

import app.fior.backend.dto.SignUpRequest
import app.fior.backend.services.LinkedInService
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
        @Id val id: String? = null,
        val name: String,
        val email: String,
        val emailValid: Boolean = false,
        val profilePicture: String? = null,
        @JsonIgnore val password: String? = null,
        val hasPassword: Boolean = false,
        @JsonIgnore val linkedInToken: Token? = null
) {
    constructor(signUpRequest: SignUpRequest) : this(
            name = signUpRequest.name,
            email = signUpRequest.email,
            password = signUpRequest.password,
            hasPassword = true
    )

    constructor(payload: GoogleIdToken.Payload) : this(
            name = payload["name"] as String,
            email = payload.email
    )

    constructor(token: Token, person: LinkedInService.LinkedInPerson) : this(
            name = "${person.firstName} ${person.lastName}",
            email = person.emailAddress ?: "",
            linkedInToken = token
    )

    fun compact() = UserCompact(id!!, name, email)

    companion object {
        fun userWithEmail(email: String) = UserCompact("", "", email)
    }
}

data class UserCompact(
        @Id val id: String?,
        val name: String,
        val email: String
)
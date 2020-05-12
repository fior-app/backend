package app.fior.backend.model

import app.fior.backend.dto.SignUpRequest
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
        @JsonIgnore val password: String?,
        val hasPassword: Boolean
) {
    constructor(signUpRequest: SignUpRequest) : this(
            name = signUpRequest.name,
            email = signUpRequest.email,
            password = signUpRequest.password,
            hasPassword = true
    )

    constructor(payload: GoogleIdToken.Payload) : this(
            name = payload["name"] as String,
            email = payload.email,
            hasPassword = false,
            password = null
    )
}
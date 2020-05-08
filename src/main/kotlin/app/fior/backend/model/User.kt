package app.fior.backend.model

import app.fior.backend.dto.SignupRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
        @Id val id: String? = null,
        val name: String,
        val email: String,
        val emailValid: Boolean = false,
        @JsonIgnore val password: String,
        val hasPassword: Boolean
) {
    constructor(signupRequest: SignupRequest) : this(
            name = signupRequest.name,
            email = signupRequest.email,
            password = signupRequest.password,
            hasPassword = true
    )
}
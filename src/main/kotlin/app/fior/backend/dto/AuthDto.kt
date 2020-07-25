package app.fior.backend.dto

import app.fior.backend.model.Role

data class SignUpRequest(
        val name: String,
        val email: String,
        var password: String
)

data class SignInEmailRequest(
        val email: String,
        val password: String,
        val scope: Role = Role.USER
)

data class SignInGoogleRequest(
        val idToken: String
)

data class SignInLinkedInRequest(
        val code: String,
        val requestUri: String
)

data class SignInResponse(
        val token: String
)

data class SignUpResponse(
        val token: String
)

data class ResetPasswordRequest(
        val token: String,
        val password: String
)

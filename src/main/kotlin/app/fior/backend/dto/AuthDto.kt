package app.fior.backend.dto

data class SignUpRequest(
        val name: String,
        val email: String,
        var password: String
)

data class SignInEmailRequest(
        val email: String,
        val password: String
)

data class SignInGoogleRequest(
        val idToken: String
)

data class SignInResponse(
        val token: String
)

data class SignUpResponse(
        val token: String
)

data class ForgotPasswordRequest(
        val email: String
)

data class ResetPasswordRequest(
        val token: String,
        val password: String
)

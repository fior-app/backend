package app.fior.backend.dto

data class LoginRequest(
        val email: String,
        val password: String
)

data class LoginResponse(
        val token: String
)

data class SignupRequest(
        val name: String,
        val email: String,
        var password: String
)

data class ForgotPasswordRequest(
        val email: String
)

data class ResetPasswordRequest(
        val password: String
)
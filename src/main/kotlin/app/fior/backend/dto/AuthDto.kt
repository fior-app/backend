package app.fior.backend.dto

data class LoginRequest(
        val username: String,
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
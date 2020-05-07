package app.fior.backend.dto

data class UpdateUserRequest(
        val name: String?,
        val email: String?
)

data class ChangePasswordRequest(
        val oldPassword: String,
        val newPassword: String
)
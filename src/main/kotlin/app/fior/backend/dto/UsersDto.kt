package app.fior.backend.dto

data class UpdateUserRequest(
        val name: String?,
        val bio: String?,
        val email: String?
)

data class ChangePasswordRequest(
        val oldPassword: String,
        val newPassword: String
)

data class ConfirmEmailRequest(
        val token: String
)

data class SetMentorRequest(
        val isMentor: Boolean
)
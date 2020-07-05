package app.fior.backend.dto

data class MessageRequest(
        val message: String,
        val senderId: String
)
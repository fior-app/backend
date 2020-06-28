package app.fior.backend.dto

data class MessageRequest(
        val roomId: String,
        val message: String
)
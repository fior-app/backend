package app.fior.backend.model.commiunication.text

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "messages")
data class Message(
        @Id val id: String? = null,
        val roomId: String,
        val message: String,
        val senderId: String
) {
    constructor(roomId: String, message: String, senderId: String) : this(
            id = null,
            roomId = roomId,
            message = message,
            senderId = senderId
    )
}
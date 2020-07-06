package app.fior.backend.model.commiunication.text

import app.fior.backend.dto.MessageRequest
import app.fior.backend.model.UserCompact
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "messages")
data class Message(
        @Id val id: String? = null,
        val roomId: String,
        val message: String,
        val sender: UserCompact
) {
    constructor(roomId: String, message: String, sender: UserCompact) : this(
            id = null,
            roomId = roomId,
            message = message,
            sender = sender
    )

    constructor(roomId: String, request: MessageRequest, sender: UserCompact) : this(
            id = null,
            roomId = roomId,
            message = request.message,
            sender = sender
    )
}
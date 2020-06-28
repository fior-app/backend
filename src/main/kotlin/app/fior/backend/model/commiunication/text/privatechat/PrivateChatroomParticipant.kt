package app.fior.backend.model.commiunication.text.privatechat

import app.fior.backend.dto.ChatRoomState
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "private_chatroom_participants")
data class PrivateChatroomParticipant(
        @Id val id: String? = null,
        val roomId: String,
        val participant1: String,
        val participant2: String,
        val state: ChatRoomState
)
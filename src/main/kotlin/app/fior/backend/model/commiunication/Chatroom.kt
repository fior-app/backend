package app.fior.backend.model.commiunication

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "chatrooms")
data class Chatroom(
        @Id val id: String? = null,
        val name: String,
        val type: ChatroomType
) {
    constructor(name: String, type: ChatroomType) : this(id = null, name = name, type = type)

    fun compact() = ChatroomCompact(id!!, name, type)

    enum class ChatroomType {
        PRIVATE,
        GROUP,
        MENTORSPACE
    }
}

data class ChatroomCompact(
        @Id val id: String,
        val name: String,
        val type: Chatroom.ChatroomType
)
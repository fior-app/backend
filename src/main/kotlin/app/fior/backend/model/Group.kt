package app.fior.backend.model

import app.fior.backend.model.commiunication.ChatroomCompact
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "groups")
data class Group(
        @Id val id: String? = null,
        val name: String,
        val description: String,
        val createdBy: UserCompact,
        val chatroom: ChatroomCompact
) {

    constructor(name: String, description: String, createdBy: UserCompact, chatroom: ChatroomCompact) : this(
            null,
            name,
            description,
            createdBy,
            chatroom
    )

}
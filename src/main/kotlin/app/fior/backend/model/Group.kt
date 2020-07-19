package app.fior.backend.model

import app.fior.backend.dto.GroupDetails
import app.fior.backend.model.commiunication.ChatroomCompact
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "groups")
data class Group(
        @Id val id: String? = null,
        val name: String,
        val description: String,
        val icon: String?,
        val createdBy: UserCompact,
        val chatroom: ChatroomCompact,
        val members: Int
) {

    constructor(name: String, description: String, icon: String?, createdBy: UserCompact, chatroom: ChatroomCompact, members: Int = 1) : this(
            null,
            name,
            description,
            icon,
            createdBy,
            chatroom,
            members
    )

    constructor(groupDetails: GroupDetails, createdBy: UserCompact, chatroom: ChatroomCompact, members: Int = 1) : this(
            null,
            groupDetails.name,
            groupDetails.description,
            groupDetails.icon,
            createdBy,
            chatroom,
            members
    )

    fun plusMember(): Group {
        return this.copy(members = this.members + 1)
    }


    fun minusMember(): Group {
        return this.copy(members = this.members - 1)
    }
}
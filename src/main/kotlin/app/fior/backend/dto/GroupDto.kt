package app.fior.backend.dto

import app.fior.backend.model.GroupMember

data class GroupDetails(
        val name: String,
        val description: String,
        val icon: String?
)

data class ProjectDetails(
        val title: String,
        val description: String,
        val github: List<String>
)

data class GroupCreateRequest(
        val group: GroupDetails
//        val project: ProjectDetails
)

data class GroupUpdateRequest(
        val name: String,
        val description: String
)

data class MemberAddRequest(
        val email: String,
        val isMentor: Boolean = false,
        val comment: String? = null
)

data class GroupStateChangeRequest(
        val state: GroupMember.GroupMemberState
)

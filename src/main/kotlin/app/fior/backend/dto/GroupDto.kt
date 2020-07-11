package app.fior.backend.dto

import app.fior.backend.model.GroupMember

data class GroupCreateRequest(
        val name: String,
        val description: String,
        val icon: String?
)

data class GroupUpdateRequest(
        val name: String,
        val description: String
)

data class MemberAddRequest(
        val email: String
)

data class GroupStateChangeRequest(
        val state: GroupMember.GroupMemberState
)
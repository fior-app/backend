package app.fior.backend.dto

import app.fior.backend.model.GroupMember

data class GroupCreateRequest(
        val name: String,
        val description: String
)

data class GroupUpdateRequest(
        val name: String,
        val description: String
)

data class MemberAddRequest(
        val memberId: String,
        val groupId: String
)

data class GroupLeaveRequest(
        val groupId: String
)

data class GroupStateChangeRequest(
        val groupId: String,
        val state: GroupMember.GroupMemberState
)
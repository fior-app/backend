package app.fior.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "group_members")
data class GroupMember(
        @Id val id: String? = null,
        val group: Group,
        var member: UserCompact,
        var state: GroupMemberState,
        val permissions: Set<Permission>,
        val comment: String? = null
) {
    enum class GroupMemberState {
        CONFIRM,
        OK,
        DECLINED
    }

    enum class Permission {
        MENTOR,
        SEND_MEMBER_REQUESTS,
        REMOVE_MEMBER,
        CLOSE_GROUP
    }

    constructor(
            group: Group,
            member: UserCompact,
            state: GroupMemberState,
            permissions: Set<Permission> = setOf(),
            comment: String? = null
    ) : this(
            null, group, member, state, permissions, comment
    )

    fun withPermission(permission: Permission) = this.copy(permissions = this.permissions.plus(permission))

    fun withoutPermission(permission: Permission) = this.copy(permissions = this.permissions.minus(permission))

    fun withAllPermissions() = this.copy(permissions = setOf(Permission.SEND_MEMBER_REQUESTS, Permission.REMOVE_MEMBER, Permission.CLOSE_GROUP))

    fun hasPermission(permission: Permission) = this.permissions.contains(permission)
}

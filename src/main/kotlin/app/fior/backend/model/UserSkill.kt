package app.fior.backend.model

import org.springframework.data.annotation.Id

data class UserSkill(
        @Id val id: String? = null,
        val userId: String,
        val skill: Skill,
        val isVerified: Boolean
) {
    constructor(user: User, skill: Skill) : this(
            null,
            user.id!!,
            skill,
            false
    )
}
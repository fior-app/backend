package app.fior.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

data class UserSkill(
        @Id val id: String? = null,
        val userId: String,
        @DBRef val skill: Skill,
        val isVerified: Boolean
) {
    constructor(user: User, skill: Skill) : this(
            null,
            user.id!!,
            skill,
            false
    )
}
package app.fior.backend.model

import app.fior.backend.dto.SkillCreateRequest
import org.springframework.data.annotation.Id

data class Skill(
        @Id val id: String? = null,
        val name: String,
        val canVerify: Boolean
) {
    constructor(skill: SkillCreateRequest) : this(
            null,
            skill.name,
            false
    )
}
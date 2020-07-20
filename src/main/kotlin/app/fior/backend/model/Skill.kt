package app.fior.backend.model

import app.fior.backend.dto.SkillCreateRequest
import app.fior.backend.dto.SkillQuestionCreateRequest
import app.fior.backend.dto.SkillQuestionUpdateRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id

data class Skill(
        @Id val id: String? = null,
        val name: String,
        val canVerify: Boolean,
        val isLinkedIn: Boolean
) {
    constructor(skill: SkillCreateRequest) : this(
            null,
            skill.name,
            false,
            false
    )
}

data class SkillQuestion(
        @Id val id: String? = null,
        val skillId: String,
        val question: String,
        val choice1: String,
        val choice2: String,
        val choice3: String,
        val choice4: String,
        @JsonIgnore val answer: Int
) {
    constructor(skillId: String, createRequest: SkillQuestionCreateRequest) : this(
            null,
            skillId,
            createRequest.question,
            createRequest.choice1,
            createRequest.choice2,
            createRequest.choice3,
            createRequest.choice4,
            createRequest.answer
    )

    fun updated(updateRequest: SkillQuestionUpdateRequest): SkillQuestion {
        return this.copy(
                question = updateRequest.question ?: this.question,
                choice1 = updateRequest.choice1 ?: this.choice1,
                choice2 = updateRequest.choice2 ?: this.choice2,
                choice3 = updateRequest.choice3 ?: this.choice3,
                choice4 = updateRequest.choice4 ?: this.choice4,
                answer = updateRequest.answer ?: this.answer
        )
    }
}

data class SkillCompact(
        @Id val id: String,
        val name: String
)
package app.fior.backend.model

import app.fior.backend.dto.QuestionCreateRequest
import app.fior.backend.dto.QuestionUpdateRequest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Question(
        @Id val id: String? = null,
        val title: String,
        val description: String,
        val votes: Int,
        val skills: List<SkillCompact>,
        val createdBy: UserCompact
) {

    constructor(question: QuestionCreateRequest, skills: List<SkillCompact>, user: UserCompact) : this(
            null,
            question.title,
            question.description,
            0,
            skills,
            user
    )

    fun updated(new: QuestionUpdateRequest, updatedSkills: List<SkillCompact>? = null): Question {
        return this.copy(
                title = new.title ?: this.title,
                description = new.description ?: this.description,
                skills = updatedSkills ?: this.skills
        )
    }

}
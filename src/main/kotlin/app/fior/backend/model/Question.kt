package app.fior.backend.model

import app.fior.backend.dto.QuestionCreateRequest
import app.fior.backend.dto.QuestionUpdateRequest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document
data class Question(
        @Id val id: String? = null,
        val title: String,
        val description: String,
        val votes: Int,
        val correctAnswer: String? = null,
        val skills: List<SkillCompact>,
        val comments: List<Comment> = listOf(),
        val createdBy: UserCompact,
        val createdAt: ZonedDateTime = ZonedDateTime.now()
) {

    constructor(question: QuestionCreateRequest, skills: List<SkillCompact>, user: UserCompact) : this(
            null,
            question.title,
            question.description,
            0,
            null,
            skills,
            listOf<Comment>(),
            user
    )

    fun updated(new: QuestionUpdateRequest, updatedSkills: List<SkillCompact>? = null): Question {
        return this.copy(
                title = new.title ?: this.title,
                description = new.description ?: this.description,
                skills = updatedSkills ?: this.skills
        )
    }

    fun withCorrectAnswer(answerId: String?): Question {
        return this.copy(
                correctAnswer = answerId
        )
    }

    fun withNewComment(comment: Comment): Question {
        return this.copy(
                comments = this.comments.plus(comment)
        )
    }

    fun withUpdatedComment(oldComment: Comment, newComment: Comment): Question {
        return this.copy(
                comments = this.comments.minus(oldComment).plus(newComment)
        )
    }

    fun withDeletedComment(comment: Comment): Question {
        return this.copy(
                comments = this.comments.minus(comment)
        )
    }

}
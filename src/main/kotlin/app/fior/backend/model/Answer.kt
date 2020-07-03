package app.fior.backend.model

import app.fior.backend.dto.AnswerCreateRequest
import app.fior.backend.dto.AnswerUpdateRequest
import app.fior.backend.dto.QuestionCreateRequest
import app.fior.backend.dto.QuestionUpdateRequest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document
data class Answer(
        @Id val id: String? = null,
        val description: String,
        val votes: Int,
        val question: String,
        val comments: List<Comment>,
        val createdBy: UserCompact,
        val createdAt: ZonedDateTime = ZonedDateTime.now()
) {

    constructor(question: String, answer: AnswerCreateRequest, user: UserCompact) : this(
            null,
            answer.description,
            0,
            question,
            listOf<Comment>(),
            user
    )

    fun updated(new: AnswerUpdateRequest): Answer {
        return this.copy(
                description = new.description
        )
    }


    fun withNewComment(comment: Comment): Answer {
        return this.copy(
                comments = this.comments.plus(comment)
        )
    }

    fun withUpdatedComment(oldComment: Comment, newComment: Comment): Answer {
        return this.copy(
                comments = this.comments.minus(oldComment).plus(newComment)
        )
    }

    fun withDeletedComment(comment: Comment): Answer {
        return this.copy(
                comments = this.comments.minus(comment)
        )
    }

}
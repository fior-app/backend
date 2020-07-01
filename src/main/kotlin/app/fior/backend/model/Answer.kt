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
        val createdBy: UserCompact,
        val createdAt: ZonedDateTime = ZonedDateTime.now()
) {

    constructor(question: String, answer: AnswerCreateRequest, user: UserCompact) : this(
            null,
            answer.description,
            0,
            question,
            user
    )

    fun updated(new: AnswerUpdateRequest): Answer {
        return this.copy(
                description = new.description
        )
    }

}
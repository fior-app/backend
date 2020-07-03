package app.fior.backend.model

import app.fior.backend.dto.CommentCreateRequest
import app.fior.backend.dto.CommentUpdateRequest
import app.fior.backend.dto.QuestionUpdateRequest
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.lang.Exception
import java.time.ZonedDateTime

@Document
data class Comment(
        @Id val id: String,
        val message: String,
        val votes: Int,
        val edited: Boolean = false,
        val createdBy: UserCompact,
        val createdAt: ZonedDateTime = ZonedDateTime.now()
) {

    constructor(comment: CommentCreateRequest, user: UserCompact) : this(
            ObjectId().toHexString(),
            comment.message,
            0,
            false,
            user
    )

    fun updated(new: CommentUpdateRequest): Comment {
        return this.copy(
                message = new.message,
                edited = true
        )
    }
}
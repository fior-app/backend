package app.fior.backend.model

import app.fior.backend.dto.PostCreateRequest
import app.fior.backend.dto.PostUpdateRequest
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document
data class Post(
        @Id val id: String? = null,
        val title: String,
        val text: String,
        val votes: Int,
        val skills: List<SkillCompact>,
        val comments: List<Comment>,
        val createdBy: UserCompact,
        val createdAt: ZonedDateTime = ZonedDateTime.now()
) {
    constructor(post: PostCreateRequest, skills: List<SkillCompact>, user: UserCompact) : this(
            null,
            post.title,
            post.text,
            0,
            skills,
            listOf<Comment>(),
            user
    )

    fun updated(new: PostUpdateRequest, updatedSkills: List<SkillCompact>? = null): Post {
        return this.copy(
                title = new.title ?: this.title,
                text = new.text ?: this.text,
                skills = updatedSkills ?: this.skills
        )
    }

    fun withNewComment(comment: Comment): Post {
        return this.copy(
                comments = this.comments.plus(comment)
        )
    }

    fun withUpdatedComment(oldComment: Comment, newComment: Comment): Post {
        return this.copy(
                comments = this.comments.minus(oldComment).plus(newComment)
        )
    }

    fun withDeletedComment(comment: Comment): Post {
        return this.copy(
                comments = this.comments.minus(comment)
        )
    }
}
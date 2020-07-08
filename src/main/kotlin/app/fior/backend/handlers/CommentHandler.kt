package app.fior.backend.handlers

import app.fior.backend.data.AnswerRepository
import app.fior.backend.data.PostRepository
import app.fior.backend.data.QuestionRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.dto.CommentCreateRequest
import app.fior.backend.dto.CommentUpdateRequest
import app.fior.backend.extensions.*
import app.fior.backend.model.Comment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty


@Component
class CommentHandler(
        private val questionRepository: QuestionRepository,
        private val answerRepository: AnswerRepository,
        private val postRepository: PostRepository,
        private val userRepository: UserRepository
) {

    fun createComment(request: ServerRequest) = when (request.queryParamOrNull("resource_type")) {
        "question" -> questionRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { question ->
                    Mono.zip(
                            request.principalUser(userRepository),
                            request.bodyToMono(CommentCreateRequest::class.java)
                    ).flatMap { (user, comment) ->
                        questionRepository.save(
                                question.withNewComment(Comment(comment, user.compact()))
                        ).flatMap {
                            "Comment created successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Question not found".toNotFoundServerResponse()
                }
        "answer" -> answerRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { answer ->
                    Mono.zip(
                            request.principalUser(userRepository),
                            request.bodyToMono(CommentCreateRequest::class.java)
                    ).flatMap { (user, comment) ->
                        answerRepository.save(
                                answer.withNewComment(Comment(comment, user.compact()))
                        ).flatMap {
                            "Comment created successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Answer not found".toNotFoundServerResponse()
                }
        "post" -> postRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { post ->
                    Mono.zip(
                            request.principalUser(userRepository),
                            request.bodyToMono(CommentCreateRequest::class.java)
                    ).flatMap { (user, comment) ->
                        postRepository.save(
                                post.withNewComment(Comment(comment, user.compact()))
                        ).flatMap {
                            "Comment created successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Question not found".toNotFoundServerResponse()
                }
        else -> "Invalid resource type".toBadRequestServerResponse()
    }

    fun updateComment(request: ServerRequest) = when (request.queryParamOrNull("resource_type")) {
        "question" -> questionRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { question ->
                    val comment = question.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        request.bodyToMono(CommentUpdateRequest::class.java)
                                .flatMap { newComment ->
                                    questionRepository.save(
                                            question.withUpdatedComment(comment, comment.updated(newComment))
                                    ).flatMap {
                                        "Comment updated successfully".toSuccessServerResponse()
                                    }
                                }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        "answer" -> answerRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { answer ->
                    val comment = answer.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        request.bodyToMono(CommentUpdateRequest::class.java)
                                .flatMap { newComment ->
                                    answerRepository.save(
                                            answer.withUpdatedComment(comment, comment.updated(newComment))
                                    ).flatMap {
                                        "Comment updated successfully".toSuccessServerResponse()
                                    }
                                }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        "post" -> postRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { post ->
                    val comment = post.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        request.bodyToMono(CommentUpdateRequest::class.java)
                                .flatMap { newComment ->
                                    postRepository.save(
                                            post.withUpdatedComment(comment, comment.updated(newComment))
                                    ).flatMap {
                                        "Comment updated successfully".toSuccessServerResponse()
                                    }
                                }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        else -> "Invalid resource type".toBadRequestServerResponse()
    }


    fun deleteComment(request: ServerRequest) = when (request.queryParamOrNull("resource_type")) {
        "question" -> questionRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { question ->
                    val comment = question.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        questionRepository.save(
                                question.withDeletedComment(comment)
                        ).flatMap {
                            "Comment Deleted successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        "answer" -> answerRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { answer ->
                    val comment = answer.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        answerRepository.save(
                                answer.withDeletedComment(comment)
                        ).flatMap {
                            "Comment Deleted successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        "post" -> postRepository.findById(request.queryParamOrNull("resource_id") ?: "")
                .flatMap { post ->
                    val comment = post.comments
                            .find { it.id == request.pathVariable("commentId") }
                            ?: return@flatMap Mono.empty<ServerResponse>()

                    request.principalUser(userRepository).flatMap principal@{ user ->
                        if (comment.createdBy.id != user.id)
                            return@principal "Unauthorized".toUnauthorizedServerResponse()

                        postRepository.save(
                                post.withDeletedComment(comment)
                        ).flatMap {
                            "Comment Deleted successfully".toSuccessServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "Comment not found".toNotFoundServerResponse()
                }
        else -> "Invalid resource type".toBadRequestServerResponse()
    }

}
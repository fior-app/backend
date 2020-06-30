package app.fior.backend.handlers

import app.fior.backend.data.QuestionRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.dto.QuestionCreateRequest
import app.fior.backend.dto.QuestionUpdateRequest
import app.fior.backend.extensions.*
import app.fior.backend.model.Question
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty


@Component
class QuestionHandler(
        private val questionRepository: QuestionRepository,
        private val userRepository: UserRepository
) {

    fun getQuestions(request: ServerRequest) = ServerResponse.ok().body(
            questionRepository.findAll()
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            Question::class.java
    )

    fun getQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("id"))
            .flatMap {
                ServerResponse.ok().bodyValue(it)
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

    fun createQuestion(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(QuestionCreateRequest::class.java)
    ).flatMap { (user, question) ->
        questionRepository.save(Question(question, user.compact())).flatMap {
            "Question created successfully".toSuccessServerResponse()
        }

    }

    fun updateQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("id"))
            .flatMap { question ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (question.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    request.bodyToMono(QuestionUpdateRequest::class.java).flatMap {
                        questionRepository.save(question.updated(it)).flatMap {
                            "Question updated successfully".toSuccessServerResponse()
                        }
                    }
                }
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

    fun deleteQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("id"))
            .flatMap { question ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (question.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    questionRepository.deleteById(question.id!!).thenReturn(true).flatMap {
                        "Question deleted successfully".toSuccessServerResponse()
                    }
                }
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

}
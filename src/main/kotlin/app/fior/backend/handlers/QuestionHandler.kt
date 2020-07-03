package app.fior.backend.handlers

import app.fior.backend.data.AnswerRepository
import app.fior.backend.data.QuestionRepository
import app.fior.backend.data.SkillRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.dto.*
import app.fior.backend.extensions.*
import app.fior.backend.model.Answer
import app.fior.backend.model.Comment
import app.fior.backend.model.Question
import app.fior.backend.model.SkillCompact
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux


@Component
class QuestionHandler(
        private val questionRepository: QuestionRepository,
        private val answerRepository: AnswerRepository,
        private val skillRepository: SkillRepository,
        private val userRepository: UserRepository
) {

    fun getQuestions(request: ServerRequest) = ServerResponse.ok().body(
            questionRepository.findAll()
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            Question::class.java
    )

    fun getQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("questionId"))
            .flatMap {
                ServerResponse.ok().bodyValue(it)
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

    fun createQuestion(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(QuestionCreateRequest::class.java)
    ).flatMap { (user, question) ->
        question.skills.toFlux().flatMap {
            skillRepository.findById(it)
        }.collectList().flatMap { skills ->
            questionRepository.save(Question(
                    question,
                    skills.map { SkillCompact(it.id!!, it.name) },
                    user.compact()
            )).flatMap {
                "Question created successfully".toSuccessServerResponse()
            }
        }
    }

    fun updateQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("questionId"))
            .flatMap { question ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (question.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    request.bodyToMono(QuestionUpdateRequest::class.java).flatMap { updateRequest ->
                        (updateRequest.skills ?: listOf()).toFlux().flatMap {
                            skillRepository.findById(it)
                        }.collectList().flatMap { skills ->
                            questionRepository.save(question.updated(
                                    updateRequest,
                                    if (skills.isEmpty()) null else skills.map { SkillCompact(it.id!!, it.name) }
                            )).flatMap {
                                "Question updated successfully".toSuccessServerResponse()
                            }
                        }
                    }
                }
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

    fun deleteQuestion(request: ServerRequest) = questionRepository.findById(request.pathVariable("questionId"))
            .flatMap { question ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (question.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    Mono.zip(
                            answerRepository.deleteAllByQuestion(question.id!!).thenReturn(true),
                            questionRepository.deleteById(question.id).thenReturn(true)
                    ).flatMap {
                        "Question deleted successfully".toSuccessServerResponse()
                    }
                }
            }.switchIfEmpty {
                "Question not found".toNotFoundServerResponse()
            }

    fun getAnswers(request: ServerRequest) = questionRepository.existsById(request.pathVariable("questionId"))
            .flatMap {
                if (!it)
                    return@flatMap "Question not found".toNotFoundServerResponse()

                ServerResponse.ok().body(
                        answerRepository.findAllByQuestion(request.pathVariable("questionId")),
                        Answer::class.java
                )
            }

    fun createAnswer(request: ServerRequest) = questionRepository.existsById(request.pathVariable("questionId"))
            .flatMap {
                if (!it)
                    return@flatMap "Question not found".toNotFoundServerResponse()

                Mono.zip(
                        request.principalUser(userRepository),
                        request.bodyToMono(AnswerCreateRequest::class.java)
                ).flatMap { (user, answer) ->
                    answerRepository.save(Answer(
                            request.pathVariable("questionId"),
                            answer,
                            user.compact()
                    )).flatMap {
                        "Answer created successfully".toSuccessServerResponse()
                    }
                }
            }

    fun updateAnswer(request: ServerRequest) = answerRepository.findById(request.pathVariable("answerId"))
            .flatMap { answer ->
                if (answer.question != request.pathVariable("questionId"))
                    return@flatMap Mono.empty<ServerResponse>()

                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (answer.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    request.bodyToMono(AnswerUpdateRequest::class.java).flatMap { updateRequest ->
                        answerRepository.save(answer.updated(updateRequest)).flatMap {
                            "Answer updated successfully".toSuccessServerResponse()
                        }
                    }
                }
            }.switchIfEmpty {
                "Answer not found".toNotFoundServerResponse()
            }

    fun deleteAnswer(request: ServerRequest) = Mono.zip(
            questionRepository.findById(request.pathVariable("questionId")),
            answerRepository.findById(request.pathVariable("answerId"))
    ).flatMap { (question, answer) ->
        if (answer.question != question.id)
            return@flatMap Mono.empty<ServerResponse>()

        request.principalUser(userRepository).flatMap principal@{ user ->
            if (answer.createdBy.id != user.id)
                return@principal "Unauthorized".toUnauthorizedServerResponse()

            answerRepository.deleteById(answer.id!!).thenReturn(true)
                    .flatMap {
                        if (question.correctAnswer == answer.id)
                            questionRepository.save(question.withCorrectAnswer(null))
                        else Mono.empty<Boolean>()
                    }.flatMap {
                        "Answer deleted successfully".toSuccessServerResponse()
                    }
        }
    }.switchIfEmpty {
        "Answer not found".toNotFoundServerResponse()
    }

    fun setCorrectAnswer(request: ServerRequest) = Mono.zip(
            questionRepository.findById(request.pathVariable("questionId")),
            answerRepository.findById(request.pathVariable("answerId"))
    ).flatMap { (question, answer) ->
        if (answer.question != question.id)
            return@flatMap Mono.empty<ServerResponse>()

        request.principalUser(userRepository).flatMap principal@{ user ->
            if (question.createdBy.id != user.id)
                return@principal "Unauthorized".toUnauthorizedServerResponse()

            questionRepository.save(question.withCorrectAnswer(answer.id!!)).flatMap {
                "Correct answer updated successfully".toSuccessServerResponse()
            }
        }
    }.switchIfEmpty {
        "Answer not found".toNotFoundServerResponse()
    }
}
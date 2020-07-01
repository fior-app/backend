package app.fior.backend.data

import app.fior.backend.model.Answer
import app.fior.backend.model.Question
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AnswerRepository : ReactiveMongoRepository<Answer, String> {

    fun findAllByQuestion(question: String): Flux<Answer>
    fun deleteAllByQuestion(question: String): Mono<Void>

}
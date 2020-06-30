package app.fior.backend.data

import app.fior.backend.model.Question
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface QuestionRepository : ReactiveMongoRepository<Question, String> {
}
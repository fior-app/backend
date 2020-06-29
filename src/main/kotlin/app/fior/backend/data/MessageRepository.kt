package app.fior.backend.data

import app.fior.backend.model.User
import app.fior.backend.model.commiunication.text.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface MessageRepository: ReactiveMongoRepository<Message, String> {
}
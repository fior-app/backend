package app.fior.backend.data

import app.fior.backend.model.commiunication.text.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface MessageRepository : ReactiveMongoRepository<Message, String> {
    fun findAllByRoomId(roomId: String): Flux<Message>
}
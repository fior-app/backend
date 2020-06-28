package app.fior.backend.data

import app.fior.backend.model.commiunication.Chatroom
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ChatroomRepository : ReactiveMongoRepository<Chatroom, String> {
}
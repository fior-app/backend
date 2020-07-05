package app.fior.backend.data

import app.fior.backend.model.Group
import app.fior.backend.model.commiunication.ChatroomCompact
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface GroupRepository : ReactiveMongoRepository<Group, String> {

    fun findByChatroom(chatroom: ChatroomCompact): Mono<Group>

}
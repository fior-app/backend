package app.fior.backend.data

import app.fior.backend.model.commiunication.text.privatechat.PrivateChatroomParticipant
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface PrivateChatroomParticipantRepository : ReactiveMongoRepository<PrivateChatroomParticipant, String> {

    @Query(value = "{ 'participant1' : ?0 , 'participant2' : ?1 }")
    fun getChatroom(participant1: String, participant2: String): Mono<PrivateChatroomParticipant>

}
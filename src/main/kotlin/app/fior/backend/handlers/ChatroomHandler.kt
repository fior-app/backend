package app.fior.backend.handlers

import app.fior.backend.data.ChatroomRepository
import app.fior.backend.data.PrivateChatroomParticipantRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ChatRoomState
import app.fior.backend.dto.ErrorResponse
import app.fior.backend.dto.PrivateChatRoomRequest
import app.fior.backend.model.commiunication.Chatroom
import app.fior.backend.model.commiunication.text.privatechat.PrivateChatroomParticipant
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.logging.Logger

@Component
class ChatroomHandler(
        private val userRepository: UserRepository,
        private val chatroomRepository: ChatroomRepository,
        private val privateChatroomParticipantRepository: PrivateChatroomParticipantRepository
) {

    fun getPrivateChatRoom(request: ServerRequest) = request.principal().flatMap { principal ->
        request.bodyToMono(PrivateChatRoomRequest::class.java).flatMap { privateChatRoomRequest ->
            userRepository.findByEmail(principal.name)
                    .flatMap { user ->
                        privateChatroomParticipantRepository.getChatroom(user.id!!, privateChatRoomRequest.allieId)
                                .flatMap { participant ->
                                    println(participant)
                                    ServerResponse.ok().bodyValue(participant)
                                }.switchIfEmpty {
                                    chatroomRepository.save(Chatroom(null,""))
                                            .flatMap {
                                                val requester = PrivateChatroomParticipant(null, it.id!!, user.id, privateChatRoomRequest.allieId, ChatRoomState.REQUEST)
                                                val receiver = PrivateChatroomParticipant(null, it.id, privateChatRoomRequest.allieId, user.id, ChatRoomState.CONFIRM)
                                                privateChatroomParticipantRepository.saveAll(listOf(requester, receiver))
                                                        .next().flatMap { participant ->
                                                            ServerResponse.ok().bodyValue(participant)
                                                        }
                                            }
                                }
                    }.switchIfEmpty {
                        ServerResponse.status(401).bodyValue(ErrorResponse("User not found"))
                    }
        }
    }

}
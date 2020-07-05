package app.fior.backend.handlers

import app.fior.backend.data.*
import app.fior.backend.dto.*
import app.fior.backend.extensions.*
import app.fior.backend.model.commiunication.Chatroom
import app.fior.backend.model.commiunication.text.Message
import app.fior.backend.model.commiunication.text.privatechat.PrivateChatroomParticipant
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.UnicastProcessor
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class ChatroomHandler(
        private val userRepository: UserRepository,
        private val chatroomRepository: ChatroomRepository,
        private val privateChatroomParticipantRepository: PrivateChatroomParticipantRepository,
        private val messagesPublisher: UnicastProcessor<Message>,
        private val messageRepository: MessageRepository,
        private val groupMemberRepository: GroupMemberRepository,
        private val groupRepository: GroupRepository
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
                                    chatroomRepository.save(Chatroom("", Chatroom.ChatroomType.PRIVATE))
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

    fun sendMessage(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(MessageRequest::class.java)
    ).flatMap { (user, msgRequest) ->
        chatroomRepository.findById(request.pathVariable("roomId"))
                .flatMap { chatroom ->
                    when (chatroom.type) {
                        Chatroom.ChatroomType.PRIVATE -> {
                            privateChatroomParticipantRepository.findByRoomIdAndParticipant1(chatroom.id!!, user.id!!).flatMap {
                                messageRepository.save(Message(request.pathVariable("roomId"), msgRequest, user.compact())).flatMap { msg ->
                                    messagesPublisher.onNext(msg)
                                    ServerResponse.ok().bodyValue(SuccessResponse("message sent!"))
                                }
                            }.switchIfEmpty {
                                ServerResponse.status(403).bodyValue(ErrorResponse("You are not participant"))
                            }
                        }
                        Chatroom.ChatroomType.GROUP -> {
                            groupRepository.findByChatroom(chatroom.compact())
                                    .flatMap { group ->
                                        groupMemberRepository.findByGroupAndMember(group, user.compact())
                                    }.flatMap { _ ->
                                        messageRepository.save(Message(request.pathVariable("roomId"), msgRequest, user.compact())).flatMap { msg ->
                                            //                                            messagesPublisher.onNext(msg)
                                            ServerResponse.ok().bodyValue(SuccessResponse("message sent!"))
                                        }
                                    }.switchIfEmpty {
                                        "You are not a participant of this room".toForbiddenServerResponse()
                                    }
                        }
                        Chatroom.ChatroomType.MENTORSPACE -> {
                            "Not support yet".toForbiddenServerResponse()
                        }
                    }
                }.switchIfEmpty {
                    "chatroom not found".toNotFoundServerResponse()
                }
    }

    fun getPrivateMessages(request: ServerRequest) = request.principal().flatMap { principal ->
        userRepository.findByEmail(principal.name)
                .flatMap { user ->
                    chatroomRepository.findById(
                            request.pathVariable("roomId")
                    ).flatMap { chatroom ->
                        privateChatroomParticipantRepository.findByRoomIdAndParticipant1(chatroom.id!!, user.id!!)
                                .flatMap { participant ->
                                    ServerResponse.ok().body(messageRepository.findAllByRoomId(participant.roomId), Message::class.java)
                                }.switchIfEmpty {
                                    ServerResponse.status(403).bodyValue(ErrorResponse("You are not participant"))
                                }
                    }.switchIfEmpty {
                        ServerResponse.status(404).bodyValue(ErrorResponse("Room not found"))
                    }
                }.switchIfEmpty {
                    ServerResponse.status(404).bodyValue(ErrorResponse("User not found"))
                }
    }

    fun getGroupMessages(request: ServerRequest) =
            Mono.zip(
                    request.principalUser(userRepository),
                    groupRepository.findById(request.pathVariable("groupId"))
            )
                    .flatMap { (user, group) ->
                        groupMemberRepository.findByGroupAndMember(group, user.compact())
                                .flatMap { _ ->
                                    ServerResponse.ok().body(messageRepository.findAllByRoomId(group.chatroom.id), Message::class.java)
                                }.switchIfEmpty {
                                    "You are not a participant of this room".toForbiddenServerResponse()
                                }
                    }.switchIfEmpty {
                        ServerResponse.status(404).bodyValue(ErrorResponse("Group not found"))
                    }
}
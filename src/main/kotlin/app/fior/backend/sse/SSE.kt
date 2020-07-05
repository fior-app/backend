package app.fior.backend.sse

import app.fior.backend.model.commiunication.text.Message
import org.reactivestreams.Publisher
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux


@RestController
class SSE(messages: Flux<Message>) {

    private val outputs = Flux.from(messages).share()

//    @GetMapping(path = ["/sse/{roomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    fun streamFlux(@PathVariable("roomId") roomId: String, principal: Principal): Publisher<Any> {
//        return userRepository.findByEmail(principal.name).flatMap { user ->
//            chatroomRepository.findById(roomId).map { chatroom ->
//                return@map if (chatroom.private) {
//                    return@map privateChatroomParticipantRepository.getPrivateChatroomParticipantsByRoomId(roomId).filter {
//                        it.participant1 == user.id
//                    }
//                            .flatMap {
//                                Flux.create<Message> { sink ->
//                                    outputs.filter { message -> message.roomId == roomId }.subscribe {
//                                        sink.next(it)
//                                    }
//                                }
//                            }.switchIfEmpty {
//                                Mono.just(ServerResponse.status(401).bodyValue(ErrorResponse("Unauthorized to grab messages")))
//                            }
//                } else {
//                    ServerResponse.noContent()
//                }
//            }.switchIfEmpty {
//                Mono.just(ServerResponse.notFound())
//            }
//        }
//    }

    @GetMapping(path = ["/sse/chatroom/{roomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFlux(@PathVariable("roomId") roomId: String): Publisher<Message> {
        return Flux.create<Message> { sink ->
            outputs.filter { message -> message.roomId == roomId }.subscribe {
                sink.next(it)
            }
        }
    }
}
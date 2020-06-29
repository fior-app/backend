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

    @GetMapping(path = ["/sse/{roomId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFlux(@PathVariable("roomId") roomId: String): Publisher<Message> = Flux.create { sink ->
        outputs.filter { message -> message.roomId == roomId }.subscribe {
            sink.next(it)
        }
    }
}
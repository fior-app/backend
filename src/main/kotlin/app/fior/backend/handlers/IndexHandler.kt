package app.fior.backend.handlers

import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class IndexHandler() {

    fun get() = ServerResponse.ok().body(Mono.just(mapOf("name" to "Fior - Rest API")), Map::class.java)

}
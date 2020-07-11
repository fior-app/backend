package app.fior.backend.extensions

import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.nio.ByteBuffer

fun ByteBuffer.toFlux(): Flux<ByteBuffer> {
    return this.toMono().toFlux()
}
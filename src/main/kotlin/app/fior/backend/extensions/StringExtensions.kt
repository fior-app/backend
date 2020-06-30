package app.fior.backend.extensions

import app.fior.backend.dto.ErrorResponse
import app.fior.backend.dto.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

fun String.toSuccessServerResponse(): Mono<ServerResponse> {
    return ServerResponse.status(HttpStatus.OK).bodyValue(SuccessResponse(this))
}

fun String.toBadRequestServerResponse(): Mono<ServerResponse> {
    return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(ErrorResponse(this))
}

fun String.toUnauthorizedServerResponse(): Mono<ServerResponse> {
    return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(ErrorResponse(this))
}

fun String.toNotFoundServerResponse(): Mono<ServerResponse> {
    return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(ErrorResponse(this))
}
package app.fior.backend.handlers

import app.fior.backend.dto.SuccessResponse
import org.springframework.web.reactive.function.server.ServerResponse

class IndexHandler() {

    fun get() = ServerResponse.ok().bodyValue(SuccessResponse("Welcome to Fior API"))

}
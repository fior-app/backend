package app.fior.backend.handlers

import app.fior.backend.dto.ApiResponse
import org.springframework.web.reactive.function.server.ServerResponse

class IndexHandler() {

    fun get() = ServerResponse.ok().bodyValue(ApiResponse("Welcome to Fior API"))

}
@file:Suppress("DuplicatedCode")

package app.fior.backend.routes

import app.fior.backend.handlers.*
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router

val routerBeans = beans {
    bean {
        router {
            val handler = IndexHandler()
            GET("/") { handler.get() }
        }
    }
}

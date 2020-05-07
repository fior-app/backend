package app.fior.backend.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository(
        private val authenticationManager: AuthenticationManager
) : ServerSecurityContextRepository {

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
    }

    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(exchange: ServerWebExchange?): Mono<SecurityContext> {
        if (exchange == null) return Mono.empty()

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val authToken = if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            authHeader.replace(TOKEN_PREFIX, "")
        } else ""

        return if (authToken.isNotEmpty()) {
            val auth = UsernamePasswordAuthenticationToken(authToken, authToken)
            this.authenticationManager.authenticate(auth).map { SecurityContextImpl(it) }
        } else {
            Mono.empty()
        }
    }

}
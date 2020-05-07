package app.fior.backend.security

import app.fior.backend.services.TokenService
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
        private val tokenService: TokenService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()

        val username = try {
            tokenService.getUsernameFromToken(authToken)
        } catch (e: Exception) {
            null
        }

        return if (username != null && !tokenService.isTokenExpired(authToken)) {
            val claims: Claims = tokenService.getAllClaimsFromToken(authToken)
            val authorities = claims.get(TokenService.SCOPES_KEY, MutableList::class.java)
                    .map { role ->
                        SimpleGrantedAuthority((role as LinkedHashMap<*, *>)["authority"] as String)
                    }
            val auth = UsernamePasswordAuthenticationToken(username, username, authorities)
            SecurityContextHolder.getContext().authentication = auth
            Mono.just(auth)
        } else {
            Mono.empty()
        }
    }
}
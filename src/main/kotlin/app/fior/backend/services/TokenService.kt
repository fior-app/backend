package app.fior.backend.services

import app.fior.backend.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
        @Value("\${fior.token.signing-key}") private val signingKey: String,
        @Value("\${fior.token.durations.access-token}") private val accessTokenDuration: Int,
        @Value("\${fior.token.durations.reset-token}") private val resetTokenDuration: Int,
        @Value("\${fior.token.durations.confirm-token}") private val confirmTokenDuration: Int
) {

    fun getUsernameFromToken(token: String?): String {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }

    fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .body
    }

    fun isTokenExpired(token: String?): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun generateAuthToken(user: User): String {
        return doGenerateToken(user.email, accessTokenDuration, TokenType.AUTH)
    }

    fun generateResetToken(user: User): String {
        return doGenerateToken(user.email, resetTokenDuration, TokenType.RESET)
    }

    fun generateConfirmToken(user: User): String {
        return doGenerateToken(user.email, confirmTokenDuration, TokenType.CONFIRM)
    }

    fun groupRequestToken(user: User): String {
        return doGenerateToken(user.email, confirmTokenDuration, TokenType.GROUP_REQUEST)
    }

    fun groupRequestToken(email: String): String {
        return doGenerateToken(email, confirmTokenDuration, TokenType.GROUP_REQUEST)
    }

    // Util functions
    private fun doGenerateToken(subject: String, duration: Int, type: TokenType): String {
        val claims = Jwts.claims()
        claims.subject = subject

        when (type) {
            TokenType.AUTH -> claims[SCOPES_KEY] = listOf(SimpleGrantedAuthority("ROLE_USER"))
            TokenType.RESET -> claims[RESET_KEY] = true
            TokenType.CONFIRM -> claims[CONFIRM_KEY] = true
            TokenType.GROUP_REQUEST -> claims[GROUP_REQUEST_KEY] = true
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + duration * 1000))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact()
    }

    private fun getExpirationDateFromToken(token: String?): Date {
        return getClaimFromToken(token) { claims: Claims -> claims.expiration }
    }

    private fun <T> getClaimFromToken(token: String?, claimsResolver: (claim: Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }

    // Util classes
    private enum class TokenType {
        AUTH, RESET, CONFIRM, GROUP_REQUEST
    }

    companion object {
        const val SCOPES_KEY = "scopes"
        const val RESET_KEY = "reset"
        const val CONFIRM_KEY = "confirm"
        const val GROUP_REQUEST_KEY = "group_request"
    }
}
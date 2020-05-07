package app.fior.backend.services

import app.fior.backend.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService {

    fun getUsernameFromToken(token: String?): String {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }

    fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .body
    }

    fun isTokenExpired(token: String?): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun generateAuthToken(user: User): String {
        return doGenerateToken(user.email!!, ACCESS_TOKEN_DURATION, TokenType.AUTH)
    }

    fun generateResetToken(user: User): String {
        return doGenerateToken(user.email!!, COMMON_TOKEN_DURATION, TokenType.RESET)
    }

    fun generateConfirmToken(user: User): String {
        return doGenerateToken(user.email!!, COMMON_TOKEN_DURATION, TokenType.CONFIRM)
    }

    // Util functions
    private fun doGenerateToken(subject: String, duration: Int, type: TokenType): String {
        val claims = Jwts.claims()
        claims.subject = subject

        when (type) {
            TokenType.AUTH -> claims[SCOPES_KEY] = listOf(SimpleGrantedAuthority("ROLE_USER"))
            TokenType.RESET -> claims[RESET_KEY] = true
            TokenType.CONFIRM -> claims[CONFIRM_KEY] = true
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + duration * 1000))
                .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
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
        AUTH, RESET, CONFIRM
    }

    companion object {
        private const val SIGNING_KEY = "jinx&accdiec"
        private const val ACCESS_TOKEN_DURATION = 28800
        private const val COMMON_TOKEN_DURATION = 3600

        const val SCOPES_KEY = "scopes"
        const val RESET_KEY = "reset"
        const val CONFIRM_KEY = "confirm"
    }
}
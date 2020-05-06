package app.fior.backend.security

import app.fior.backend.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*


object TokenProvider {
    private const val SIGNING_KEY = "jinx&accdiec"
    private const val ACCESS_TOKEN_VALIDITY_SECONDS = 28800

    const val SCOPES_KEY = "scopes"

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

    fun generateToken(user: User): String {
        return doGenerateToken(user.email!!)
    }

    // Util functions
    private fun doGenerateToken(subject: String): String {
        val claims = Jwts.claims().setSubject(subject)
        claims[SCOPES_KEY] = listOf(SimpleGrantedAuthority("ROLE_USER"))

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
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
}
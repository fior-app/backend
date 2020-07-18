package app.fior.backend.services

import app.fior.backend.model.Token
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class LinkedInService(
        @Value("\${fior.auth.linkedin-client-id}")
        private val linkedinClientId: String,
        @Value("\${fior.auth.linkedin-client-secret}")
        private val linkedinClientSecret: String
) {

    fun getAccessToken(code: String, redirectUri: String): Mono<LinkedInToken> {
        return WebClient.create("https://www.linkedin.com/oauth/v2/accessToken")
                .post()
                .bodyValue(LinkedInTokenRequest(
                        "authorization_code",
                        code,
                        redirectUri,
                        linkedinClientId,
                        linkedinClientSecret
                ))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .flatMap { it.bodyToMono(LinkedInToken::class.java) }
    }

    fun getMe(accessToken: String): Mono<LinkedInPerson> {
        return WebClient.create("https://api.linkedin.com/v2/me")
                .get()
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .flatMap { it.bodyToMono(LinkedInPerson::class.java) }
    }

    // DTOs
    data class LinkedInTokenRequest(
            val grant_type: String,
            val code: String,
            val redirect_uri: String,
            val client_id: String,
            val client_secret: String
    )

    data class LinkedInToken(
            @JsonProperty("access_token") override val accessToken: String,
            @JsonProperty("expires_in") override val expiresIn: String
    ) : Token(accessToken, expiresIn)

    data class LinkedInPerson(
            val id: String,
            @JsonProperty("localizedFirstName")
            val firstName: String,
            @JsonProperty("localizedLastName")
            val lastName: String,
            @JsonProperty("localizedHeadline")
            val headline: String,
            val vanityName: String,
            val email: String
    )
}
package app.fior.backend.services

import app.fior.backend.model.Token
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class LinkedInService(
        @Value("\${fior.auth.linkedin-client-id}")
        private val linkedinClientId: String,
        @Value("\${fior.auth.linkedin-client-secret}")
        private val linkedinClientSecret: String
) {

    fun getAccessToken(code: String, redirectUri: String): Mono<Token> {
        return WebClient.create("https://www.linkedin.com/oauth/v2/accessToken")
                .post()
                .uri {
                    it.queryParam("grant_type", "authorization_code")
                            .queryParam("code", code)
                            .queryParam("redirect_uri", redirectUri)
                            .queryParam("client_id", linkedinClientId)
                            .queryParam("client_secret", linkedinClientSecret)
                            .build()
                }
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .flatMap { response ->
                    response.bodyToMono(LinkedInToken::class.java).flatMap token@{
                        if (it.accessToken != null && it.expiresIn != null)
                            return@token Mono.just(Token(it.accessToken, it.expiresIn))

                        Mono.empty<Token>()
                    }
                }
    }

    fun getMe(accessToken: String): Mono<LinkedInPerson> {
        return WebClient.create("https://api.linkedin.com/v2/me")
                .get()
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .flatMap { it.bodyToMono(LinkedInPerson::class.java) }
    }

    fun getMyEmail(accessToken: String): Mono<String> {
        return WebClient.create("https://api.linkedin.com/v2/clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))")
                .get()
                .header("Authorization", "Bearer $accessToken")
                .exchange()
                .flatMap {
                    it.bodyToMono(LinkedInElements::class.java).flatMap elements@{ elementsResponse ->
                        if (elementsResponse.elements.isNotEmpty()) {
                            val handle = elementsResponse.elements.first().handle
                                    ?: return@elements Mono.empty<String>()
                            return@elements Mono.just(handle.emailAddress)
                        }

                        Mono.empty<String>()
                    }
                }
    }

    data class LinkedInToken(
            @JsonProperty("access_token") val accessToken: String?,
            @JsonProperty("expires_in") val expiresIn: String?,
            @JsonProperty("error") val error: String?,
            @JsonProperty("error_description") val errorDescription: String?
    )

    data class LinkedInPerson(
            val id: String?,
            @JsonProperty("localizedFirstName")
            val firstName: String?,
            @JsonProperty("localizedLastName")
            val lastName: String?,
            @JsonProperty("localizedHeadline")
            val headline: String?,
            val vanityName: String?,
            val emailAddress: String?
    )

    data class LinkedInElements(
            val elements: List<LinkedInEmailElement>
    ) {

        data class LinkedInEmailElement(
                @JsonProperty("handle~")
                val handle: LinkedInEmailHandle?,
                @JsonProperty("handle!")
                val handleError: LinkedInHandleError?,
                val primary: Boolean,
                val type: String
        )

        data class LinkedInEmailHandle(
                val emailAddress: String
        )

        data class LinkedInHandleError(
                val status: Int
        )
    }
}
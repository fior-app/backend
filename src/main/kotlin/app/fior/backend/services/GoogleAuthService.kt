package app.fior.backend.services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class GoogleAuthService {

    @Value("app.fior.auth.google.client.id")
    private lateinit var googleClientId: String
    @Autowired
    private lateinit var idTokenVerifier: GoogleIdTokenVerifier

    fun verifyIdToken(idTokenString: String): Mono<GoogleIdToken.Payload> {
        return Mono.justOrEmpty(idTokenString).map {
            val payload = idTokenVerifier.verify(idTokenString)?.payload
            payload
        }
    }

    @Bean
    fun googleIdTokenVerifier(): GoogleIdTokenVerifier {
        return GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build()
    }
}
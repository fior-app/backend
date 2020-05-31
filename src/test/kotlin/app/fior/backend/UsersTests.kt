package app.fior.backend

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.SignUpRequest
import app.fior.backend.model.User
import app.fior.backend.services.TokenService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class UsersTests {

    @Autowired
    private lateinit var client: WebTestClient
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    lateinit var passwordEncoder: BCryptPasswordEncoder
    @Autowired
    lateinit var tokenService: TokenService
    private val logger = LoggerFactory.getLogger(AuthTests::class.java)

    private lateinit var token: String

    @BeforeAll
    fun init() {
        val user = createUser("fior user", "user@fior.app", "pass123@")
        userRepository.save(user).then().subscribe {
            logger.info("a user created!")
        }
        token = tokenService.generateAuthToken(user)
    }

    // User me tests
    @Test
    fun getUserMe() {

        client.get().uri("/users/me").accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
    }

    @Test
    fun getUserMeNotFoundUser() {
        val noUser = createUser("fior no user", "no-user@fior.app", "pass123@")
        val fakeToken = tokenService.generateAuthToken(noUser)
        client.post().uri("/users/me").accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $fakeToken")
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$").exists()
    }

    fun createUser(name: String, email: String, password: String): User {
        val request = SignUpRequest(name, email, password)
        request.password = passwordEncoder.encode(request.password)
        return User(request)
    }

}
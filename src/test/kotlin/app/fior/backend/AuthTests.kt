package app.fior.backend

import app.fior.backend.data.UserRepository
import app.fior.backend.dto.ForgotPasswordRequest
import app.fior.backend.dto.ResetPasswordRequest
import app.fior.backend.dto.SignInEmailRequest
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
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class AuthTests {

    @Autowired
    private lateinit var client: WebTestClient
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    lateinit var passwordEncoder: BCryptPasswordEncoder
    @Autowired
    lateinit var tokenService: TokenService
    private val logger = LoggerFactory.getLogger(AuthTests::class.java)

    @BeforeAll
    fun init() {
        val user = createUser("fior user", "user@fior.app", "pass123@")
        userRepository.save(user).then().subscribe {
            logger.info("a user created!")
        }
    }

    // signUp tests
    @Test
    fun postSignUpSuccess() {
        val request = SignUpRequest("fior user", "user2@fior.app", "pass123@")

        client.post().uri("/auth/signup").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.message").isEqualTo("User created successfully")
    }

    @Test
    fun postSignUpAllReadyExist() {
        val request = SignUpRequest("fior user", "user@fior.app", "pass123@")

        client.post().uri("/auth/signup").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("User already exist")
    }

    // sign in request
    @Test
    fun postSignInEmailSuccess() {
        val request = SignInEmailRequest("user@fior.app", "pass123@")

        client.post().uri("/auth/signin/email").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.token").exists()
    }

    @Test
    fun postSignInEmailInvalidCredentials() {
        val request = SignInEmailRequest("user@fior.app", "pass123@+")

        client.post().uri("/auth/signin/email").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid credentials")
    }


    @Test
    fun postSignInEmailNoUser() {
        val request = SignInEmailRequest("no-user@fior.app", "pass123@")

        client.post().uri("/auth/signin/email").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody()
                .jsonPath("$.message").isEqualTo("User does not exist")
    }

    // ForgotPassword tests
    @Test
    fun postForgotPasswordSuccess() {
        val request = ForgotPasswordRequest("user@fior.app")

        client.post().uri("/auth/forgotPassword").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.message").isEqualTo("Password reset email sent")
    }

    @Test
    fun postForgotPasswordUserNotFound() {
        val request = ForgotPasswordRequest("user3@fior.app")

        client.post().uri("/auth/forgotPassword").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found")
    }

    // check reset password tests
    @Test
    fun getCheckResetPasswordSuccess() {

        Mono.just(generateResetPasswordToken("fior user", "user@fior.app", "pass123@")).map { token ->
            client.get().uri("/auth/resetPassword?token=${token}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$").exists()
        }
    }

    @Test
    fun getCheckResetPasswordUserNotFound() {

        Mono.just(generateResetPasswordToken("fior user", "no-user@fior.app", "pass123@")).map { token ->
            client.get().uri("/auth/resetPassword?token=${token}")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("User not found")
        }
    }

    @Test
    fun getCheckResetPasswordTokenNotValid() {

        Mono.just(generateResetPasswordToken("fior user", "user@fior.app", "pass123@")).map { token ->
            client.get().uri("/auth/resetPassword?token=${token}abc")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Reset token is not valid")
        }
    }

    @Test
    fun getCheckResetPasswordParamNotFound() {

        Mono.just(generateResetPasswordToken("fior user", "user@fior.app", "pass123@")).map { _ ->
            client.get().uri("/auth/resetPassword")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Token query parameter is not found")
        }
    }

    // reset password test
    @Test
    fun postResetPasswordSuccess() {

        Mono.just(generateResetPasswordToken("fior user", "user@fior.app", "pass123@")).map { token ->
            ResetPasswordRequest(token, "newPassword")
        }.map { request ->
            client.post().uri("/auth/resetPassword")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Password reset successfully")
        }
    }

    @Test
    fun postResetPasswordUserNotFound() {

        Mono.just(generateResetPasswordToken("fior user", "no-user@fior.app", "pass123@")).map { token ->
            ResetPasswordRequest(token, "newPassword")
        }.map { request ->
            client.post().uri("/auth/resetPassword")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("User not found")
        }
    }

    @Test
    fun postResetPassword() {
        Mono.just(generateResetPasswordToken("fior user", "user@fior.app", "pass123@")).map { token ->
            ResetPasswordRequest(token, "newPassword")
        }.map { request ->
            client.post().uri("/auth/resetPassword")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Token query parameter is not found")
        }
    }

    fun generateResetPasswordToken(name: String, email: String, password: String): String {
        val user = createUser(name, email, password)
        return tokenService.generateResetToken(user)
    }

    fun createUser(name: String, email: String, password: String): User {
        val request = SignUpRequest(name, email, password)
        request.password = passwordEncoder.encode(request.password)
        return User(request)
    }
}
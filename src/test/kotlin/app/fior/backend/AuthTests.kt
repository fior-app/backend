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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

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

    @BeforeAll
    fun init() {
        val user1 = createUser("fior user", "user1@fior.app", "pass123@")
        val user2 = createUser("fior user", "user2@fior.app", "pass123@")
        userRepository.save(user1).subscribe()
        userRepository.save(user2).subscribe()
    }

    // signUp tests
    @Test
    fun postSignUpSuccess() {
        val request = SignUpRequest("fior user", "user3@fior.app", "pass123@")

        client.post().uri("/auth/signup").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.message").isEqualTo("User created successfully")
    }

    @Test
    fun postSignUpAllReadyExist() {
        val request = SignUpRequest("fior user", "user1@fior.app", "pass123@")

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
        val request = SignInEmailRequest("user1@fior.app", "pass123@")

        client.post().uri("/auth/signin/email").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.token").exists()
    }

    @Test
    fun postSignInEmailInvalidCredentials() {
        val request = SignInEmailRequest("user1@fior.app", "pass123@+")

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
        val request = ForgotPasswordRequest("user1@fior.app")

        client.post().uri("/auth/forgotPassword").accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.message").isEqualTo("Password reset email sent")
    }

    @Test
    fun postForgotPasswordUserNotFound() {
        val request = ForgotPasswordRequest("no-user@fior.app")

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

        val token = generateResetPasswordToken("fior user", "user2@fior.app", "pass123@")
        client.get().uri("/auth/resetPassword?token=${token}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
    }

    @Test
    fun getCheckResetPasswordUserNotFound() {

        val token = generateResetPasswordToken("fior user", "no-user@fior.app", "pass123@")
        client.get().uri("/auth/resetPassword?token=${token}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found")
    }

    @Test
    fun getCheckResetPasswordTokenNotValid() {

        // TODO: apply invalid token
        val token = generateResetPasswordToken("fior user", "user2@fior.app", "pass123@")

//        client.get().uri("/auth/resetPassword?token=${token}abc")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isBadRequest
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Reset token is not valid")
    }

    @Test
    fun getCheckResetPasswordParamNotFound() {
        val token = generateResetPasswordToken("fior user", "user2@fior.app", "pass123@")
        client.get().uri("/auth/resetPassword")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Token query parameter is not found")
    }

    // reset password test
    @Test
    fun postResetPasswordSuccess() {

        val token = generateResetPasswordToken("fior user", "user2@fior.app", "pass123@")
        val request = ResetPasswordRequest(token, "newPassword")

        client.post().uri("/auth/resetPassword")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.message").isEqualTo("Password reset successfully")
    }

    @Test
    fun postResetPasswordUserNotFound() {

        val token = generateResetPasswordToken("fior user", "no-user@fior.app", "pass123@")
        val request = ResetPasswordRequest(token, "newPassword")

        client.post().uri("/auth/resetPassword")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found")
    }

    @Test
    fun postResetPasswordTokenNotValid() {
        val token = generateResetPasswordToken("fior user", "user2@fior.app", "pass123@")
        // TODO: apply invalid token
        val request = ResetPasswordRequest(token, "newPassword")

//        client.post().uri("/auth/resetPassword")
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isBadRequest
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Reset token is not valid")
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
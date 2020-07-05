package app.fior.backend

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class UsersTests {

//    @Autowired
//    private lateinit var client: WebTestClient
//    @Autowired
//    private lateinit var userRepository: UserRepository
//    @Autowired
//    lateinit var passwordEncoder: BCryptPasswordEncoder
//    @Autowired
//    lateinit var tokenService: TokenService
//
//    private lateinit var token1: String
//    private lateinit var token2: String
//
//    @BeforeAll
//    fun init() {
//        val user1 = createUser("fior user", "user1@fior.app", "pass123@")
//        val user2 = createUser("fior user", "user2@fior.app", "pass123@")
//        userRepository.save(user1).subscribe()
//        userRepository.save(user2).subscribe()
//        token1 = tokenService.generateAuthToken(user1)
//        token2 = tokenService.generateAuthToken(user2)
//    }
//
//    // User me tests
//    @Test
//    fun getUserMeSuccess() {
//
//        client.get().uri("/users/me").accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer $token1")
//                .exchange()
//                .expectStatus().isOk
//                .expectBody()
//                .jsonPath("$").exists()
//    }
//
//    @Test
//    fun getUserMeNotFoundUser() {
//        val noUser = createUser("fior no user", "no-user@fior.app", "pass123@")
//        val fakeToken = tokenService.generateAuthToken(noUser)
//        client.post().uri("/users/me").accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer $fakeToken")
//                .exchange()
//                .expectStatus().isNotFound
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("User not found")
//    }
//
//    // update user tests
//    @Test
//    fun postUserUpdateMeSuccess() {
//
//        val updateRequest = UpdateUserRequest("new name", "new-mail@fior.app")
//
//        client.post().uri("/users/me").accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer $token2")
//                .bodyValue(updateRequest)
//                .exchange()
//                .expectStatus().isOk
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("User updated successfully")
//    }
//
//    @Test
//    fun postUserUpdateMeNotFoundUser() {
//        val updateRequest = UpdateUserRequest("new name", "no-user@fior.app")
//
//        val noUser = createUser("fior no user", "no-user@fior.app", "pass123@")
//        val fakeToken = tokenService.generateAuthToken(noUser)
//        client.post().uri("/users/me").accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer $fakeToken")
//                .bodyValue(updateRequest)
//                .exchange()
//                .expectStatus().isNotFound
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("User not found")
//    }
//
//    @Test
//    fun postUserUpdateMeEmailAlreadyExist() {
//
//        val updateRequest = UpdateUserRequest("new name", "user1@fior.app")
//
//        client.post().uri("/users/me").accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer $token2")
//                .bodyValue(updateRequest)
//                .exchange()
//                .expectStatus().isBadRequest
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("User with given email already exists")
//    }
//
//    fun createUser(name: String, email: String, password: String): User {
//        val request = SignUpRequest(name, email, password)
//        request.password = passwordEncoder.encode(request.password)
//        return User(request)
//    }

}
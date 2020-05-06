package app.fior.backend.data

import app.fior.backend.model.User
import org.springframework.data.repository.CrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface UserRepository : CrudRepository<User, Int> {

    fun findByEmail(email: String): Optional<User>

}
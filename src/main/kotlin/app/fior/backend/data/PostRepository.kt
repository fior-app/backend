package app.fior.backend.data

import app.fior.backend.model.Post
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface PostRepository : ReactiveMongoRepository<Post, String> {

    fun findAllByCreatedBy_Id(userId: String): Flux<Post>

}

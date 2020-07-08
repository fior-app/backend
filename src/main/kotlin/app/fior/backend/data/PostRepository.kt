package app.fior.backend.data

import app.fior.backend.model.Post
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PostRepository : ReactiveMongoRepository<Post, String> {
}
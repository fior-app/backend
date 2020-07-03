package app.fior.backend.data

import app.fior.backend.model.Group
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface GroupRepository : ReactiveMongoRepository<Group, String> {
}
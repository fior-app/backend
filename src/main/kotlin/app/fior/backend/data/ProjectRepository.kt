package app.fior.backend.data

import app.fior.backend.model.Project
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProjectRepository : ReactiveMongoRepository<Project, String> {
}
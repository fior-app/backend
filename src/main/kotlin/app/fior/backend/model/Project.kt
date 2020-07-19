package app.fior.backend.model

import app.fior.backend.dto.ProjectDetails
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Projects")
data class Project(
        @Id val id: String? = null,
        val title: String,
        val description: String,
        val github: List<String>,
        val mentorSpaceId: String
) {
    constructor(project: ProjectDetails, mentorSpaceId: String) : this(
            id = null,
            title = project.title,
            description = project.description,
            github = project.github,
            mentorSpaceId = mentorSpaceId
    )
}
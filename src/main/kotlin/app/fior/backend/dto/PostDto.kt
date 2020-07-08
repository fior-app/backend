package app.fior.backend.dto

data class PostCreateRequest(
        val title: String,
        val text: String,
        val skills: List<String>
)

data class PostUpdateRequest(
        val title: String?,
        val text: String?,
        val skills: List<String>?
)
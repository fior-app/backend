package app.fior.backend.dto

data class QuestionCreateRequest(
        val title: String,
        val description: String,
        val skills: List<String>
)

data class QuestionUpdateRequest(
        val title: String?,
        val description: String?,
        val skills: List<String>?
)
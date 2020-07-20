package app.fior.backend.dto

data class SkillCreateRequest(
        val name: String
)

data class SkillQuestionCreateRequest(
        val question: String,
        val choice1: String,
        val choice2: String,
        val choice3: String,
        val choice4: String,
        val answer: Int
)

data class SkillQuestionUpdateRequest(
        val question: String?,
        val choice1: String?,
        val choice2: String?,
        val choice3: String?,
        val choice4: String?,
        val answer: Int?
)


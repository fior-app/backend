package app.fior.backend.dto

import app.fior.backend.model.SkillQuestion

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

data class SkillQuestionFullResponse(
        val id: String,
        val question: String,
        val choice1: String,
        val choice2: String,
        val choice3: String,
        val choice4: String,
        val answer: Int
) {
    constructor(skillQuestion: SkillQuestion) : this(
            skillQuestion.id!!,
            skillQuestion.question,
            skillQuestion.choice1,
            skillQuestion.choice2,
            skillQuestion.choice3,
            skillQuestion.choice4,
            skillQuestion.answer
    )
}

data class SkillQuestionAnswersRequest(
        val answers: List<SkillQuestionAnswer>
) {
    data class SkillQuestionAnswer(
            val questionId: String,
            val answer: Int
    )
}
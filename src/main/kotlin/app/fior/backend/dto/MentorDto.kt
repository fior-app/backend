package app.fior.backend.dto

import app.fior.backend.model.User
import app.fior.backend.model.UserSkill

data class Mentor(
        val user: User,
        val skills: List<UserSkill>
)

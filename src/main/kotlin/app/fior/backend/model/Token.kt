package app.fior.backend.model

open class Token(
        open val accessToken: String,
        open val expiresIn: String
)
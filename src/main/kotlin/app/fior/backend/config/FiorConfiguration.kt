package app.fior.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "fior")
data class FiorConfiguration(
        val auth: AuthConfiguration,
        val token: TokenConfiguration,
        val sendgrid: SendGridConfiguration,
        val storage: StorageConfiguration
) {
    data class AuthConfiguration(
            val googleClientId: String,
            val linkedinClientId: String,
            val linkedinClientSecret: String
    )

    data class TokenConfiguration(
            val signingKey: String,
            val durations: TokenDurationsConfiguration
    ) {
        data class TokenDurationsConfiguration(
                var accessToken: Int,
                var resetToken: Int,
                var confirmToken: Int
        )
    }

    data class SendGridConfiguration(
            val apiKey: String,
            val sender: String,
            val templates: SendGridTemplateConfiguration
    ) {
        data class SendGridTemplateConfiguration(
                val forgotPassword: String,
                val emailConfirmation: String,
                val groupInvitation: String
        )
    }

    data class StorageConfiguration(
            val accountName: String,
            val accountKey: String,
            val useEmulatorval: Boolean = false,
            val emulatorBlobHost: String?,
            val containerName: String,
            val enableHttps: Boolean = false
    )
}
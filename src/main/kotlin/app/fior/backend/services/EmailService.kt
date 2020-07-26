package app.fior.backend.services

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class EmailService(
        @Value("\${fior.sendgrid.api-key}") private val apiKey: String,
        @Value("\${fior.sendgrid.sender}") private val sender: String,
        @Value("\${fior.sendgrid.templates.email-confirmation}") private val emailConfirmationTemplate: String,
        @Value("\${fior.sendgrid.templates.forgot-password}") private val forgotPasswordTemplate: String,
        @Value("\${fior.sendgrid.templates.group-invitation}") private val groupInvitationTemplate: String
) {

    fun sendEmailConfirmation(email: String, token: String): Mono<Int> {
        val personalization = Personalization()
        personalization.addDynamicTemplateData("token", token)
        personalization.addTo(Email(email))

        return sendMail(emailConfirmationTemplate, personalization)
    }

    fun sendForgotPassword(email: String, token: String): Mono<Int> {
        val personalization = Personalization()
        personalization.addDynamicTemplateData("token", token)
        personalization.addTo(Email(email))

        return sendMail(forgotPasswordTemplate, personalization)
    }

    fun sendGroupInvitation(email: String, token: String): Mono<Int> {
        val personalization = Personalization()
        personalization.addDynamicTemplateData("link", "http://localhost:3000/groups/invitation/${token}")
        personalization.addTo(Email(email))

        return sendMail(groupInvitationTemplate, personalization)
    }

    private fun sendMail(templateId: String, personalization: Personalization): Mono<Int> {
        val mail = Mail()
        mail.setFrom(Email(sender))
        mail.setTemplateId(templateId)

        mail.addPersonalization(personalization)

        val sg = SendGrid(apiKey)
        val request = Request()

        return Mono.fromCallable {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response: Response = sg.api(request)

            response.statusCode
        }
    }

}
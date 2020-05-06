package app.fior.backend.services

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import reactor.core.publisher.Mono

object EmailService {
    private const val SENDGRID_API_KEY = "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"

    private const val EMAIL_NO_REPLY = "noreply@fior.app"

    private const val TEMP_FORGOT_PASSWORD = "d-f13c22e0126d4909a3cdb6d5af14ac14"
    private const val TEMP_EMAIL_CONFIRMATION = "d-9bbb6721ed274fcbae3b6d369db3074b"

    fun sendEmailConfirmation(email: String, token: String): Mono<Int> {
        val personalization = Personalization()
        personalization.addDynamicTemplateData("token", token)
        personalization.addTo(Email(email))

        return sendMail(TEMP_EMAIL_CONFIRMATION, personalization)
    }

    fun sendForgotPassword(email: String, token: String): Mono<Int> {
        val personalization = Personalization()
        personalization.addDynamicTemplateData("token", token)
        personalization.addTo(Email(email))

        return sendMail(TEMP_FORGOT_PASSWORD, personalization)
    }

    private fun sendMail(templateId: String, personalization: Personalization): Mono<Int> {
        val mail = Mail()
        mail.setFrom(Email(EMAIL_NO_REPLY))
        mail.setTemplateId(templateId)

        mail.addPersonalization(personalization)

        val sg = SendGrid(SENDGRID_API_KEY)
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
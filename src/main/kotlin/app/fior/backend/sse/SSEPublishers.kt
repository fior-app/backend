package app.fior.backend.sse

import app.fior.backend.model.commiunication.text.Message
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.UnicastProcessor

@Configuration
class SSEPublishers {

    @Bean
    fun messagesPublisher(): UnicastProcessor<Message> = UnicastProcessor.create()

    @Bean
    fun messages(eventPublisher: UnicastProcessor<Message>) = eventPublisher.replay(25).autoConnect()

}
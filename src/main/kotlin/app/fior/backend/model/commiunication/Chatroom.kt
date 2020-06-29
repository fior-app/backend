package app.fior.backend.model.commiunication

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "chatrooms")
data class Chatroom(
        @Id val id: String? = null,
        val name: String,
        val private:Boolean
) {
    constructor(name: String,private: Boolean) : this(id = null, name = name,private = private)
}
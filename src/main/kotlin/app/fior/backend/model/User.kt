package app.fior.backend.model

import app.fior.backend.dto.SignupRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType


@Entity
class User() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null

    var name: String? = null

    var email: String? = null

    var emailValid: Boolean = false

    @JsonIgnore
    var password: String? = null

    constructor(signupRequest: SignupRequest) : this() {
        name = signupRequest.name
        email = signupRequest.email
        password = signupRequest.password
    }
}
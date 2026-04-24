package pt.isel.ls.domain

import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.EmailSerial

data class User(
    val uid: Int,
    val name: String,
    val email: Email,
    val password: String,
    val token: String,
)

class Email(
    val email: String,
) {
    init {
        if (!email.contains("@")) {
            throw InternalError.InvalidBody("Invalid email")
        }
    }
}

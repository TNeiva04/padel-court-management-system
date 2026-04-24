package pt.isel.ls.http.Web.API.models
import kotlinx.serialization.Serializable


@Serializable
data class UserInputCreateUser(val name: String, val email: String, val password: String)

@Serializable
data class UserDetails(val uid: Int, val name: String, val email: EmailSerial, val password: String, val token: String)

@Serializable
data class UserOutputCreateUser(val uid: Int, val token: String)

@Serializable
data class UserOutputGetDetails(val uid: Int, val name: String, val email: EmailSerial,val token: String="")

@Serializable
data class UserOutputName(val name: String)

@Serializable
data class UserOutputWithID(val uid: Int)

@Serializable
data class UserOutputWithToken(val uid: Int, val name: String)

@Serializable
data class UserInputLogin(val email: String, val password: String)


@Serializable
data class EmailSerial(val email: String) {
    init {
        if (!email.contains("@")) {
            throw IllegalArgumentException("Invalid email")
        }
    }
}
package pt.isel.ls.data.DataMem

import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.domain.User
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*
import java.util.*

val token1 = UUID.randomUUID().toString()
val token2 = UUID.randomUUID().toString()
val token3 = UUID.randomUUID().toString()

val Users =
    mutableListOf(
        User(1, "Francisco Cunha", Email("fc@email.com"), "pass123", token1),
        User(2, "Tiago Neiva", Email("tv@email.com"), "pass456", token2),
        User(3, "André Carrilho", Email("ac@email.com"), "pass789", token3),
    )

class InMemoryUserRepository : UserRepository {
    override fun createUser(
        name: String,
        email: Email,
        password: String,
    ): UserOutputCreateUser {
        val uid = Users.size + 1
        val token = UUID.randomUUID().toString()
        val user = User(uid, name, email,password, token)
        Users.add(user)
        return UserOutputCreateUser(uid, token)
    }

    override fun checkEmailNotExists(email: Email): Boolean = Users.none { it.email == email }

    override fun getUserById(uid: Int): UserOutputGetDetails {
        val user = Users.find { it.uid == uid } ?: throw InternalError.UserNotFound
        return UserOutputGetDetails(
            uid = user.uid,
            name = user.name,
            email = EmailSerial(user.email.email),
            token = user.token,
        )
    }

    override fun getUserByToken(token: String): UserOutputWithToken =
        Users.find { it.token == token }?.let { user ->
            UserOutputWithToken(uid = user.uid, name = user.name)
        } ?: throw InternalError.UserNotFound

    override fun getUserID(name: String): UserOutputWithID {
        val user = Users.find { it.name == name } ?: throw InternalError.UserNotFound
        return UserOutputWithID(uid = user.uid)
    }

    override fun tokenVerify(
        token: String,
        uid: Int,
    ): Boolean {
        val user = Users.find { it.token == token } ?: throw throw InternalError.UserNotFound
        return user.token == token
    }

    override fun getAllUsers(): List<UserOutputGetDetails> =
        Users.map { user ->
            UserOutputGetDetails(
                uid = user.uid,
                name = user.name,
                email = EmailSerial(user.email.toString()),
                token = user.token,
            )
        }

    override fun tokenVerify_forCourt(
        token: String,
        clubId: Int,
    ): Boolean {
        val user = Users.find { it.token == token } ?: throw InternalError.UserNotFound
        val club = Clubs.find { it.cid == clubId } ?: return false
        return club.owner.uid == user.uid
    }

    override fun tokenVerify_forRentalUpdate(
        user: Int,
        rid: Int,
    ): Boolean {
        val rental = Rentals.find { it.rid == rid } ?: return false
        return rental.uid.uid == user
    }

    override fun loginUser(email: String, password: String): UserOutputCreateUser {
        val user = Users.find { it.email.email == email && it.password == password }
            ?: throw InternalError.InvalidBody("Invalid email or password")
        return UserOutputCreateUser(uid = user.uid, token = user.token)
    }
}

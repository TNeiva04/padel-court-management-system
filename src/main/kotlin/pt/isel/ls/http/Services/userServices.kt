package pt.isel.ls.http.Services

import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*

class UserServices(
    private val userRepo: UserRepository,
) {
    fun getAllUsersService(): List<UserOutputGetDetails> = userRepo.getAllUsers()

    fun findUserService(uid: Int): UserOutputGetDetails {
        require(uid > 0) { throw InternalError.InvalidParameter("Invalid UserId") }
        return userRepo.getUserById(uid)
    }

    fun createUserService(
        name: String,
        email: Email,
        password: String
    ): UserOutputCreateUser {
        if (name.isBlank()) {
            throw InternalError.InvalidBody("missing name")
        }
        if (!userRepo.checkEmailNotExists(email)) {
            throw InternalError.EmailAlreadyUsed
        }
        if (password.isBlank()) {
            throw InternalError.InvalidBody("missing password")
        }

        return userRepo.createUser(name, email, password)
    }

    fun loginUserService(
        email: String,
        password: String
    ): UserOutputCreateUser {
        require(email.isNotBlank()) { throw InternalError.InvalidBody("Email cannot be blank") }
        require(password.isNotBlank()) { throw InternalError.InvalidBody("Password cannot be blank") }
        val user = userRepo.loginUser(email, password)
        if (user.token.isBlank()) {
            throw InternalError.InvalidBody("Login failed, invalid credentials")
        }
        return user
    }
}

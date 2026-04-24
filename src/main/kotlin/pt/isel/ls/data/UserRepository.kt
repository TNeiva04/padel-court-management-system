package pt.isel.ls.data

import pt.isel.ls.domain.Email
import pt.isel.ls.http.Web.API.models.*

interface UserRepository {
    fun createUser(
        name: String,
        email: Email,
        password: String
    ): UserOutputCreateUser

    fun getUserById(uid: Int): UserOutputGetDetails

    fun checkEmailNotExists(email: Email): Boolean

    fun getUserByToken(token: String): UserOutputWithToken

    fun getUserID(name: String): UserOutputWithID

    fun tokenVerify(
        token: String,
        uid: Int,
    ): Boolean

    fun getAllUsers(): List<UserOutputGetDetails>

    fun tokenVerify_forCourt(
        token: String,
        clubId: Int,
    ): Boolean

    fun tokenVerify_forRentalUpdate(
        uid: Int,
        rid: Int,
    ): Boolean

    fun loginUser(
        email: String,
        password: String
    ): UserOutputCreateUser

    interface UserRepositoryDB : UserRepository {
        fun dbGetUserName(uid: Int): UserOutputName

        fun dbGetUserDetailsByID(uid: Int): UserOutputGetDetails
    }

}

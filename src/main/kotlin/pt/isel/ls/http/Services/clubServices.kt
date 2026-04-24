package pt.isel.ls.http.Services

import pt.isel.ls.data.ClubRepository
import pt.isel.ls.data.UserRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*

class ClubServices(
    private val clubRepo: ClubRepository,
    private val userRepo: UserRepository,
) {
    fun createClubService(
        name: String,
        token: String,
    ): ClubOutput {
        val userId = userRepo.getUserByToken(token)
        require(name.isNotBlank()) { throw InternalError.InvalidBody("missing name") }

        return clubRepo.createClub(
            name,
            userId.name,
        ) ?: throw InternalError.InvalidBody("missing court")
    }

    fun listUserClubsService(token: String, uid: Int): ClubList {
        require(userRepo.getUserByToken(token).uid == uid) {
            throw InternalError.InvalidParameter("Invalid UserId")
        }
        return clubRepo.getClubsOfaUser(uid)
    }

    fun getClubByIdService(cid: Int): ClubOutputWithIDCompleted {
        require(cid > 0) { throw InternalError.InvalidParameter("Invalid ClubId") }
        return clubRepo.getClubDetails(cid)
    }

    fun listAllClubsService(name: String?): ClubList =
        if (!name.isNullOrBlank()) {
            clubRepo.getClubsByName(name)
        } else {
            clubRepo.getAllClubs()
        }
}

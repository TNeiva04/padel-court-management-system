package pt.isel.ls.http.Services

import pt.isel.ls.data.RentalRepository
import pt.isel.ls.data.UserRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*
import pt.isel.ls.http.Web.API.stringToTimestamp2

class RentalServices(
    private val rentalRepo: RentalRepository,
    private val userRepo: UserRepository,
) {
    fun createRentalService(
        cid: Int,
        crid: Int,
        date: String,
        duration: Int,
        token: String,
    ): OutputRental {
        val user = userRepo.getUserByToken(token)
        println(user)
        require(cid > 0) { throw InternalError.InvalidParameter("clubId") }
        require(crid > 0) { throw InternalError.InvalidParameter("courtId") }
        require(date.isNotBlank()) { throw InternalError.InvalidBody("date") }
        require(duration in 0..23) { throw InternalError.InvalidBody("duration") }
        require(user.uid > 0) { throw InternalError.InvalidParameter("userId") }

        return rentalRepo.createRental(cid, crid, stringToTimestamp2(date), duration, user.uid)
    }

    fun getRentalsService(): List<RentalOutput> = rentalRepo.getRentals()

    fun getRentalByIdService(rid: Int): RentalOutput {
        require(rid > 0) { throw InternalError.InvalidParameter("rentalId") }
        return rentalRepo.getRentalById(rid)
    }

    fun getRentalsByUserService(token: String): List<RentalOutput> {
        val user = userRepo.getUserByToken(token)
        require(user.uid > 0) { throw InternalError.InvalidParameter("Invalid user ID") }

        return rentalRepo.getRentalsByUser(user.uid)
    }

    fun getRentalsByClubCourtDateService(
        cid: Int,
        crid: Int,
        date: String,
    ): List<RentalOutput> {
        require(cid > 0) { throw InternalError.InvalidParameter("Invalid club ID") }
        require(crid > 0) { throw InternalError.InvalidParameter("Invalid court ID") }
        require(date.isNotBlank()) { throw InternalError.InvalidParameter("date") }

        return rentalRepo.getRentalsByClubCourtDate(cid, crid, stringToTimestamp2(date))
    }

    fun getAvailableHoursService(
        cid: Int,
        crid: Int,
        date: String,
    ): HoursList {
        require(cid > 0) { throw InternalError.InvalidParameter("Invalid club ID") }
        require(crid > 0) { throw InternalError.InvalidParameter("Invalid court ID") }
        require(date.isNotBlank()) { throw InternalError.InvalidBody("Invalid date") }

        return rentalRepo.getAvailableHours(cid, crid, stringToTimestamp2(date))
    }

    fun deleteRentalService(rid: Int, token: String): Boolean {
        require(rid > 0) { throw InternalError.InvalidParameter("rental ID") }

        return rentalRepo.deleteRental(rid, token)
    }

    fun updateRentalService(
        rid: Int,
        date: String,
        duration: Int,
        token: String,
    ): RentalOutput {
        val user = userRepo.getUserByToken(token)
        if (!userRepo.tokenVerify_forRentalUpdate(user.uid, rid)) {
            throw InternalError.NotAuthorized
        }
        require(rid > 0) { throw InternalError.InvalidParameter("rental ID") }
        require(date.isNotBlank()) { throw InternalError.InvalidBody("date") }
        require(duration in 0..23) { throw InternalError.InvalidBody("duration") }

        return rentalRepo.updateRental(rid, stringToTimestamp2(date), duration)
    }
}

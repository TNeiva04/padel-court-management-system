package pt.isel.ls.http.Services

import pt.isel.ls.data.CourtRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*

class CourtServices(
    private val courtRepo: CourtRepository,
) {
    fun createCourtService(
        name: String,
        cid: Int,
    ): CourtOutputWithID {
        require(name.isNotBlank()) { throw InternalError.MissingParameter }
        require(cid > 0) { throw InternalError.InvalidParameter("Invalid ClubId") }
        return courtRepo.createCourt(name, cid)
    }

    fun getCourtByIdService(crid: Int): CourtOutputCompleted {
        require(crid > 0) { throw InternalError.InvalidParameter("Invalid CourtId") }
        return courtRepo.getCourtById(crid)
    }

    fun getCourtsByClubService(cid: Int): CourtList {
        require(cid > 0) { throw InternalError.InvalidParameter("Invalid ClubId") }
        return courtRepo.getCourtsByClub(cid)
    }
}

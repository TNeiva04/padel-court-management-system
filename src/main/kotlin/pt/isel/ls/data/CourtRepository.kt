package pt.isel.ls.data

import pt.isel.ls.http.Web.API.models.*

interface CourtRepository {
    fun createCourt(
        name: String,
        cid: Int,
    ): CourtOutputWithID

    fun getCourtById(crid: Int): CourtOutputCompleted

    fun getCourtsByClub(cid: Int): CourtList

    fun getCourtId(name: String): CourtOutputWithID
}

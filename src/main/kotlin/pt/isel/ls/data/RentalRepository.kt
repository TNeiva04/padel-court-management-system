package pt.isel.ls.data

import pt.isel.ls.http.Web.API.models.*
import java.sql.Timestamp

interface RentalRepository {
    fun createRental(
        cid: Int,
        crid: Int,
        datein: Timestamp,
        duration: Int,
        uid: Int
    ): OutputRental

    fun getRentals(): List<RentalOutput>

    fun getRentalById(rid: Int): RentalOutput
    fun getRentalsByClubCourtDate(cid: Int, crid: Int, date: Timestamp): List<RentalOutput>
    fun getRentalsByUser(uid: Int): List<RentalOutput>
    fun getAvailableHours(cid: Int, crid: Int, date: Timestamp): HoursList
    fun deleteRental(rid: Int, token: String): Boolean
    fun updateRental(rid: Int, date: Timestamp, duration: Int): RentalOutput
}
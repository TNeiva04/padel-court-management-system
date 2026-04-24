package pt.isel.ls.data.DataMem

import pt.isel.ls.data.CourtRepository
import pt.isel.ls.domain.Club
import pt.isel.ls.domain.Court
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*

val Courts = mutableListOf(
    Court(1, "Court_1", Clubs[0]),
    Court(2, "Court_2", Clubs[1]),
    Court(3, "Court_3", Clubs[2])
)

class InMemoryCourtRepository : CourtRepository {

    override fun createCourt(name: String, cid: Int): CourtOutputWithID {
        val club = Clubs.find { it.cid == cid }
            ?: throw InternalError.ClubNotFound("Club with ID $cid not found.")

        val newCrid = (Courts.maxOfOrNull { it.crid } ?: 0) + 1
        val court = Court(newCrid, name, Club(cid, club.name, club.owner))
        Courts.add(court)

        return CourtOutputWithID(crid = court.crid)
    }

    override fun getCourtById(crid: Int): CourtOutputCompleted {
        val court = Courts.find { it.crid == crid }
            ?: throw InternalError.CourtNotFound("Court with ID ${crid} not found.")

        return CourtOutputCompleted(
            crid = court.crid,
            name = court.name,
            club = ClubOutputWithIDCompleted(
                cid = court.club.cid,
                name = court.club.name,
                owner = UserOutputGetDetails(
                    uid = court.club.owner.uid,
                    name = court.club.owner.name,
                    email = EmailSerial(court.club.owner.email.toString())
                )
            )
        )
    }

    override fun getCourtsByClub(cid: Int): CourtList {
        val courts = Courts
            .filter { it.club.cid == cid }
            .map { court ->
                CourtOutputCompleted(
                    crid = court.crid,
                    name = court.name,
                    club = ClubOutputWithIDCompleted(
                        cid = court.club.cid,
                        name = court.club.name,
                        owner = UserOutputGetDetails(
                            uid = court.club.owner.uid,
                            name = court.club.owner.name,
                            email = EmailSerial(court.club.owner.email.toString())
                        )
                    )
                )
            }

        return CourtList(courts)
    }


    override fun getCourtId(name: String): CourtOutputWithID {
        val court = Courts.find { it.name == name }
            ?: throw InternalError.CourtNotFound("Court with name ${name} not found.")

        return CourtOutputWithID(crid = court.crid)
    }
}

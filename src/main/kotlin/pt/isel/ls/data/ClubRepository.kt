package pt.isel.ls.data

import pt.isel.ls.http.Web.API.models.*

interface ClubRepository {
    fun createClub(name: String, owner: String): ClubOutput

    fun getClubID(club: String): ClubOutput

    fun getClubsOfaUser(userId: Int): ClubList

    fun getClubDetails(club: Int): ClubOutputWithIDCompleted

    fun getAllClubs(): ClubList

    fun getClubsByName(name: String): ClubList


    interface ClubRepositoryBD : ClubRepository {

        fun String.isNotInDB(): Boolean
    }


}




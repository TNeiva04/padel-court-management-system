package pt.isel.ls.data.DataMem

import pt.isel.ls.data.ClubRepository
import pt.isel.ls.domain.Club
import pt.isel.ls.domain.User
import pt.isel.ls.http.Web.API.models.*
import pt.isel.ls.error.InternalError

val Clubs =
    mutableListOf(
        Club(1, "Club_1", Users[0]),
        Club(2, "Club_2", Users[1]),
        Club(3, "Club_3", Users[2]),
    )

class InMemoryClubRepository : ClubRepository {

    override fun createClub(name: String, owner: String): ClubOutput {
        val cid = Clubs.size + 1
        val owner = InMemoryUserRepository().getUserID(owner)
        val ownerDetails = InMemoryUserRepository().getUserById(owner.uid)
        val user = Users.find { it.uid == ownerDetails.uid }
            ?: throw InternalError.UserNotFound
        val club = Club(cid, name, user)
        Clubs.add(club)
        return ClubOutput(cid)
    }


    override fun getClubDetails(club: Int): ClubOutputWithIDCompleted {
        val club1 = Clubs.find { it.cid == club } ?: throw InternalError.ClubNotFound("$club")

        return ClubOutputWithIDCompleted(
            cid = club1.cid,
            name = club1.name,
            owner =
                UserOutputGetDetails(
                    uid = club1.owner.uid,
                    name = club1.owner.name,
                    email = EmailSerial(club1.owner.email.toString())
                ),
        )
    }

    override fun getClubsOfaUser(userId: Int): ClubList {
        val owner = InMemoryUserRepository().getUserById(userId)
        val clubs =
            Clubs
                .filter { it.owner.uid == owner.uid }
                .map { club ->
                    ClubOutputWithIDCompleted(
                        club.cid,
                        club.name,
                        UserOutputGetDetails(
                            uid = club.owner.uid,
                            name = club.owner.name,
                            email = EmailSerial(club.owner.email.toString())
                        ),
                    )
                }

        return ClubList(clubs)
    }

    override fun getClubID(club: String): ClubOutput {
        val club1 = Clubs.find { it.name == club }
            ?: throw InternalError.ClubNotFound("Club with name ${club} not found.")
        return ClubOutput(club1.cid)
    }

    override fun getAllClubs(): ClubList {
        val clubs =
            Clubs.map { club ->
                ClubOutputWithIDCompleted(
                    club.cid,
                    club.name,
                    UserOutputGetDetails(
                        uid = club.owner.uid,
                        name = club.owner.name,
                        email = EmailSerial(club.owner.email.toString())
                    ),
                )
            }
        return ClubList(clubs)
    }

    override fun getClubsByName(name: String): ClubList {
        val clubs =
            Clubs.filter { it.name.contains(name, ignoreCase = true) }
                .map { club ->
                    ClubOutputWithIDCompleted(
                        club.cid,
                        club.name,
                        UserOutputGetDetails(
                            uid = club.owner.uid,
                            name = club.owner.name,
                            email = EmailSerial(club.owner.email.toString())
                        ),
                    )
                }
        return ClubList(clubs)
    }
}
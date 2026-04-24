package pt.isel.ls.http.Web.API.models

import kotlinx.serialization.Serializable


@Serializable
data class ClubInput(val name: String)

@Serializable
data class ClubOutput(val cid: Int)

@Serializable
data class ClubOutputWithIDCompleted(val cid: Int, val name: String, val owner: UserOutputGetDetails)

@Serializable
data class ClubList(val clubs: List<ClubOutputWithIDCompleted>) {
    fun drop(skip: Int): ClubList {
        return ClubList(clubs.drop(skip))
            }
    fun take(take: Int): ClubList {
        return ClubList(clubs.take(take))
    }
}


package pt.isel.ls.http.Web.API.models

import kotlinx.serialization.Serializable


@Serializable
data class CourtInput(val name: String)



@Serializable
data class CourtOutputWithID(val crid: Int)

@Serializable
data class CourtOutputCompleted(val crid: Int, val name: String, val club: ClubOutputWithIDCompleted)


@Serializable
data class CourtList(val courts: List<CourtOutputCompleted>) {
    fun drop(skip: Int): CourtList {
        return CourtList(courts.drop(skip))
    }

    fun take(take: Int): CourtList {
        return CourtList(courts.take(take))
    }
}


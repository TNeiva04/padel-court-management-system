//package pt.isel.ls.phase_1.httpTests
//
//import kotlinx.serialization.json.Json
//import org.http4k.core.Method.GET
//import org.http4k.core.Method.POST
//import org.http4k.core.Request
//import org.http4k.core.Status
//import org.http4k.routing.bind
//import org.http4k.routing.routes
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import pt.isel.ls.http.Web.API.models.*
//import pt.isel.ls.http.Web.API.getClubById
//import pt.isel.ls.http.Web.API.createClub
//import pt.isel.ls.http.Web.API.getClubs
//import pt.isel.ls.http.Web.API.listUserClubs
//
//class clubTests {
//    @Test
//    fun get_club_with_ID_1() {
//        val app = routes(
//            "clubs/{cid}" bind GET to ::getClubById,
//            "clubs" bind POST to ::createClub,
//            "users/{uid}/clubs" bind GET to ::listUserClubs,
//            "clubs" bind GET to ::getClubs
//        )
//
//        val response = app(Request(GET, "/clubs/2"))
//
//        assertEquals(Status.OK, response.status)
//
//        val clubDetails = Json.decodeFromString<ClubOutputWithIDCompleted>(response.bodyString())
//
//        assertEquals(
//            ClubOutputWithIDCompleted(2, "Benfica", UserOutputGetDetails(1, "Tiago Neiva", EmailSerial("neiva@gmail.com"), "token")),
//            clubDetails
//        )
//    }
//
//    @Test
//    fun create_club() {
//        val app = routes(
//            "clubs/{cid}" bind GET to ::getClubById,
//            "clubs" bind POST to ::createClub,
//            "users/{uid}/clubs" bind GET to ::listUserClubs,
//            "clubs" bind GET to ::getClubs
//        )
//
//        val requestBody = Json.encodeToString(ClubInput("example_club_name"))
//        val request = Request(POST, "/clubs")
//            .header("Content-Type", "application/json")
//            .body(requestBody)
//
//        val response = app(request)
//
//        assertEquals(Status.CREATED, response.status)
//
//        val clubDetailsResponse = Json.decodeFromString<ClubOutput>(response.bodyString())
//        assertEquals(
//            ClubOutput(clubDetailsResponse.cid),
//            clubDetailsResponse
//        )
//    }
//
////    @Test
////    fun list_user_clubs() {
////        val app = routes(
////            "clubs/{cid}" bind GET to ::getClubById,
////            "clubs" bind POST to ::createClub,
////            "users/{uid}/clubs" bind GET to ::listUserClubs,
////            "clubs" bind GET to ::getClubs
////        )
////
////        val response = app(Request(GET, "/users/1/clubs"))
////
////        assertEquals(Status.OK, response.status)
////
////        val userClubs = Json.decodeFromString<ClubList>(response.bodyString())
////
////        assertEquals(
////
////        )
////    }
//
//}
//

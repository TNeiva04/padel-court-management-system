//package pt.isel.ls.http
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
//import pt.isel.ls.http.Web.*
//import pt.isel.ls.http.Web.API.createUser
//import pt.isel.ls.http.Web.API.getUserById
//import pt.isel.ls.http.Web.API.models.*
//
//class userTests {
//
//    @Test
//    fun get_user_with_ID_1() {
//        val app = routes(
//            "users/{uid}" bind GET to ::getUserById,
//            "users" bind POST to ::createUser
//        )
//
//        val response = app(Request(GET, "/users/2"))
//
//        assertEquals(Status.OK, response.status)
//
//        val userDetails = Json.decodeFromString<UserOutputGetDetails>(response.bodyString())
//
//        assertEquals(
//            UserOutputGetDetails(2, "Francisco Cunha", EmailSerial("fc@example.com"), "d0d8f2a2-c6c8-46bc-817e-2fee68f355bd"),
//            userDetails
//        )
//    }
//
//    @Test
//    fun create_user() {
//        val app = routes(
//            "users/{uid}" bind GET to ::getUserById,
//            "users" bind POST to ::createUser
//        )
//
//        val requestBody = Json.encodeToString(UserInputCreateUser("example_username", "example1@example.com")) // Email válido
//        val request = Request(POST, "/users")
//            .header("Content-Type", "application/json")
//            .body(requestBody)
//
//        val response = app(request)
//        println("Response Status: ${response.status}")
//        println("Response Body: ${response.bodyString()}")
//
//        assertEquals(Status.CREATED, response.status) // Agora deve retornar 201 Created
//
//        val userDetailsResponse = Json.decodeFromString<UserOutputCreateUser>(response.bodyString())
//        assertEquals(
//            UserOutputCreateUser(userDetailsResponse.uid, userDetailsResponse.token),
//            userDetailsResponse
//        )
//    }
//
//
//
//
//}
package pt.isel.ls.http.Web.API

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.path
import pt.isel.ls.error.InternalError
import pt.isel.ls.error.handleError
import pt.isel.ls.http.Server.*
import pt.isel.ls.http.Services.*
import pt.isel.ls.http.Web.API.models.*

class ClubWebApi(
    private val clubServices: ClubServices,
) {
    fun createClub(request: Request): Response {
        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        logRequest(request)

        return try {
            val club = Json.decodeFromString<ClubInput>(request.bodyString())
            val createdClub = clubServices.createClubService(club.name, token)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(createdClub))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getClubById(request: Request): Response {
        logRequest(request)

        return try {
            val cid =
                request.path("cid")?.toIntOrNull()
                    ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")
            val club = clubServices.getClubByIdService(cid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(club))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getClubs(request: Request): Response {
        logRequest(request)
        val nameQuery = request.query("name")
        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        val clubs = clubServices.listAllClubsService(nameQuery)

        val paginatedClubs = clubs.drop(skip).take(limit)
        return Response(OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(paginatedClubs))
    }

    fun listUserClubs(request: Request): Response {
        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        logRequest(request)
        val uid =
            request.path("uid")?.toIntOrNull()
                ?: return Response(BAD_REQUEST).body("Missing or invalid User ID")

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        val clubs = clubServices.listUserClubsService(token, uid)

        val paginatedClubs = clubs.drop(skip).take(limit)
        return Response(OK)
            .header("content-type", "application/json")
            .body(Json.encodeToString(paginatedClubs))
    }
}

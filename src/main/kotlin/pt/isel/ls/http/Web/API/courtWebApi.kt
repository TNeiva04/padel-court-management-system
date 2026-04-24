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

class CourtWebApi(
    private val courtServices: CourtServices,
) {
    fun createCourt(request: Request): Response {
        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        val court = Json.decodeFromString<CourtInput>(request.bodyString())
        val clubId =
            request.path("cid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")

        logRequest(request)

        return try {
            if (!tokenVerifyForCourt(token, clubId, userRepo)) {
                println("Not valid")
                throw InternalError.NotAuthorized
            }
            val createdCourt = courtServices.createCourtService(court.name, clubId)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(createdCourt))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getCourtById(request: Request): Response {
        logRequest(request)

        return try {
            val crid =
                request.path("crid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid court ID")
            val court = courtServices.getCourtByIdService(crid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(court))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getCourtsByClub(request: Request): Response {
        logRequest(request)

        val clubId =
            request.path("cid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        return try {
            val courts = courtServices.getCourtsByClubService(clubId)
            val paginatedCourts = courts.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedCourts))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }
}

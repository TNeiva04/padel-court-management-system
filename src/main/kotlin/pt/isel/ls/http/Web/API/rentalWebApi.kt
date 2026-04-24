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

class RentalWebApi(
    private val rentalServices: RentalServices,
) {
    fun createRental(request: Request): Response {
        logRequest(request)
        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        val clubId =
            request.path("cid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")
        val courtId =
            request.path("crid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid court ID")

        return try {
            val rental = Json.decodeFromString<RentalInput>(request.bodyString())
            val createdRental =
                rentalServices.createRentalService(clubId, courtId, rental.date, rental.Duration.duration, token)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(createdRental))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getRentals(request: Request): Response {
        logRequest(request)

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        return try {
            val rentals = rentalServices.getRentalsService()
            val paginatedRentals = rentals.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedRentals))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getRentalById(request: Request): Response {
        logRequest(request)
        val rid =
            request.path("rid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid rental ID")

        return try {
            val rental = rentalServices.getRentalByIdService(rid)
            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(rental))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getUserRentalsById(request: Request): Response {
        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        logRequest(request)

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        return try {
            val rentals = rentalServices.getRentalsByUserService(token)
            val paginatedRentals = rentals.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedRentals))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getRentalsByClubCourtDate(request: Request): Response {
        logRequest(request)

        val cid = request.path("cid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")
        val crid =
            request.path("crid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid court ID")
        val date = request.query("date") ?: return Response(BAD_REQUEST).body("Missing or invalid date")

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        return try {
            val rentals = rentalServices.getRentalsByClubCourtDateService(cid, crid, date)
            val paginatedRentals = rentals.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedRentals))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getAvailableHours(request: Request): Response {
        logRequest(request)

        val crid =
            request.path("crid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid court ID")
        val cid = request.path("cid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid club ID")
        val date = request.query("date") ?: return Response(BAD_REQUEST).body("Missing or invalid date")

        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

        return try {
            val hours = rentalServices.getAvailableHoursService(cid, crid, date)
            val paginatedHours = hours.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedHours))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun deleteRental(request: Request): Response {
        logRequest(request)

        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        val rid =
            request.path("rid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid rental ID")

        return try {
            rentalServices.deleteRentalService(rid, token)
            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString("Rental deleted successfully"))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun updateRental(request: Request): Response {
        logRequest(request)

        val token = getToken(request) ?: return Response(UNAUTHORIZED).body("Missing authentication token")
        val rid =
            request.path("rid")?.toIntOrNull() ?: return Response(BAD_REQUEST).body("Missing or invalid rental ID")

        return try {
            val rental = Json.decodeFromString<RentalInput>(request.bodyString())
            val updatedRental = rentalServices.updateRentalService(rid, rental.date, rental.Duration.duration, token)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(updatedRental))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }
}

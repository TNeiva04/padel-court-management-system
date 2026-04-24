package pt.isel.ls.error

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.*
import org.http4k.core.Response

@Serializable
sealed class InternalError(
    val code: Int,
    val description: String,
) : Throwable(description) {

    data class InvalidCredentials(val argName: String): InternalError(0, "Invalid credentials")

    object MissingParameter : InternalError(1, "Missing parameter")

    data class InvalidParameter(
        val argName: String,
    ) : InternalError(2, "Invalid parameter $argName")

    data class InvalidBody(
        val argName: String,
    ) : InternalError(3, "Invalid body $argName")

    data class NameNotFound(
        val what: String,
    ) : InternalError(4, "Nmae $what not found")

    data class ClubNotFound(
        val what: String,
    ) : InternalError(4, "Club $what not found")

    data class CourtNotFound(
        val what: String,
    ) : InternalError(4, "Court $what not found")

    data class RentalNotFound(
        val what: String,
    ) : InternalError(4, "Rental $what not found")

    data class CourtAlreadyExists(
        val what: String,
    ) : InternalError(4, " $what ")

    object UserNotFound : InternalError(5, "User not found")

    object ClubAlreadyExists : InternalError(1, "Club name already exists")

    object NotAuthorized : InternalError(6, "Not Authorized")

    object MissingToken : InternalError(7, "Missing token")

    object EmailAlreadyUsed : InternalError(8, "Email is already used")
}

fun handleError(e: InternalError): Response =
    when (e) {
        is InternalError.MissingParameter -> errorToHttp(e)
        is InternalError.InvalidParameter -> errorToHttp(e)
        is InternalError.InvalidBody -> errorToHttp(e)
        is InternalError.EmailAlreadyUsed -> errorToHttp(e)
        is InternalError.ClubNotFound -> errorToHttp(e)
        is InternalError.CourtNotFound -> errorToHttp(e)
        is InternalError.RentalNotFound -> errorToHttp(e)
        is InternalError.UserNotFound -> errorToHttp(e)
        is InternalError.NotAuthorized -> errorToHttp(e)
        is InternalError.MissingToken -> errorToHttp(e)
        else -> errorToHttp(InternalError.InvalidBody("Unknown error"))
    }

@Serializable
data class ErrorResponse(
    val code: Int,
    val error: String,
)

fun errorToHttp(e: InternalError): Response {
    val status =
        when (e) {
            is InternalError.ClubAlreadyExists -> Status.CONFLICT
            is InternalError.EmailAlreadyUsed -> Status.CONFLICT
            is InternalError.MissingParameter,
            is InternalError.InvalidParameter,
            is InternalError.InvalidBody,
                -> Status.BAD_REQUEST

            is InternalError.ClubNotFound,
            is InternalError.CourtNotFound,
            is InternalError.RentalNotFound,
                -> Status.NOT_FOUND

            is InternalError.UserNotFound -> Status.NOT_FOUND
            is InternalError.NotAuthorized -> Status.UNAUTHORIZED
            is InternalError.MissingToken -> Status.UNAUTHORIZED
            else -> Status.INTERNAL_SERVER_ERROR
        }
    return Response(status)
        .header("content-type", "application/json")
        .body(Json.encodeToString(ErrorResponse(e.code, e.description)))
}

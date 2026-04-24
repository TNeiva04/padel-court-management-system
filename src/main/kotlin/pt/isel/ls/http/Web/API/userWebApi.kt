package pt.isel.ls.http.Web.API

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import pt.isel.ls.domain.Email
import pt.isel.ls.error.*
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Server.*
import pt.isel.ls.http.Services.* // the new unified services
import pt.isel.ls.http.Web.API.models.*

class UserWebApi(
    private val userServices: UserServices,
) {
    fun getAllUsers(request: Request): Response {
        logRequest(request)

        return try {
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

            val users = userServices.getAllUsersService()
            val paginatedUsers = users.drop(skip).take(limit)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(paginatedUsers))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun getUserById(request: Request): Response {
        logRequest(request)

        return try {
            val uid =
                request.path("uid")?.toIntOrNull()
                    ?: return Response(BAD_REQUEST).body("Missing or invalid user ID")

            val user = userServices.findUserService(uid)

            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(user))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun createUser(request: Request): Response {
        logRequest(request)

        return try {
            val user = Json.decodeFromString<UserInputCreateUser>(request.bodyString())
            val createdUser = userServices.createUserService(user.name, Email(user.email), user.password)

            Response(CREATED)
                .header("content-type", "application/json")
                .body(Json.encodeToString(createdUser))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }

    fun Userlogin(request: Request): Response {
        logRequest(request)

        return try {
            val user = Json.decodeFromString<UserInputLogin>(request.bodyString())
            val token = userServices.loginUserService(user.email, user.password)


            Response(OK)
                .header("content-type", "application/json")
                .body(Json.encodeToString(token))
        } catch (e: InternalError) {
            handleError(e)
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Invalid request")
        }
    }
}

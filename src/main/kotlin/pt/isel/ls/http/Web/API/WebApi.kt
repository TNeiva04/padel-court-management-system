package pt.isel.ls.http.Web.API

import kotlinx.datetime.Clock
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import pt.isel.ls.data.UserRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Server.*
import java.net.URLDecoder
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun getToken(req: Request): String? {
    val authToken = req.header("Authorization")
    return authToken?.split(" ")?.takeIf { it.size == 2 && it[0] == "Bearer" }?.get(1)
}

fun getDate(request: Request): Response =
    Response(OK).header("content-type", "text/plain").body(Clock.System.now().toString())


fun stringToTimestamp2(dateString: String): Timestamp {
    try {
        println("data $dateString")

        val decodedDateString = URLDecoder.decode(dateString, "UTF-8")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        dateFormat.isLenient = false

        val parsedDate =
            dateFormat.parse(decodedDateString)
                ?: throw InternalError.InvalidBody(" $dateString")

        val cal = Calendar.getInstance()
        cal.time = parsedDate

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        if (year !in 1900..2100) throw InternalError.InvalidBody("Ano inválido: $year")
        if (month !in 1..12) throw InternalError.InvalidBody("Mês inválido: $month")
        if (day !in 1..31) throw InternalError.InvalidBody("Dia inválido: $day")

        return Timestamp(parsedDate.time)
    } catch (e: ParseException) {
        e.printStackTrace()
        throw InternalError.InvalidBody(" $dateString")
    }
}

fun tokenVerifyUser(
    token: String,
    uid: Int,
    userRepo: UserRepository,
): Boolean {
    require(token.isNotBlank()) { "Token cannot be empty" }
    require(uid > 0) { "Invalid user ID" }
    return userRepo.tokenVerify(token, uid)
}

fun tokenVerifyForCourt(
    token: String,
    uid: Int,
    userRepo: UserRepository,
): Boolean {
    require(token.isNotBlank()) { "Token cannot be empty" }
    require(uid > 0) { "Invalid user ID" }
    return userRepo.tokenVerify_forCourt(token, uid)
}

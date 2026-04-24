package pt.isel.ls.http.Server

import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.routing.*
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import pt.isel.ls.data.ClubRepository
import pt.isel.ls.data.CourtRepository
import pt.isel.ls.data.DataMem.*
import pt.isel.ls.data.RentalRepository
import pt.isel.ls.data.UserRepository
import pt.isel.ls.data.db.*
import pt.isel.ls.http.Services.*
import pt.isel.ls.http.Web.API.*

var dataMem = false // True = DataMem, False = DataBase

private val logger = LoggerFactory.getLogger("pt.isel.ls.http.courtsServer")

fun logRequest(request: Request) {
    logger.info(
        "incoming request: method={}, uri={}, content-type={} accept={} ",
        request.method,
        request.uri,
        request.header("content-type"),
        request.header("accept"),
    )
}

val dataSource =
    PGSimpleDataSource().apply {
        setURL("jdbc:postgresql://localhost/postgres?user=postgres&password=1234")
        //setURL("jdbc:postgresql://dpg-d1pt0jc9c44c73901hjg-a.frankfurt-postgres.render.com:5432/db_ls_2425_2_43d_g07_v6of?user=db_ls_2425_2_43d_g07_v6of_user&password=iwhiDE1JWuquFOJd1J2LicQMyvCmFc2d")
    }

val userRepo: UserRepository =
    if (dataMem) {
        InMemoryUserRepository()
    } else {
        DatabaseUserRepository(dataSource)
    }

val clubRepo: ClubRepository =
    if (dataMem) {
        InMemoryClubRepository()
    } else {
        DatabaseClubRepository(dataSource)
    }

val courtRepo: CourtRepository =
    if (dataMem) {
        InMemoryCourtRepository()
    } else {
        DatabaseCourtRepository(dataSource)
    }

val rentalsrepo: RentalRepository =
    if (dataMem) {
        InMemoryRentalRepository()
    } else {
        DatabaseRentalRepository(dataSource)
    }

val userServices = UserServices(userRepo)
val clubServices = ClubServices(clubRepo, userRepo)
val courtServices = CourtServices(courtRepo)
val rentalServices = RentalServices(rentalsrepo, userRepo)

val userWebApi = UserWebApi(userServices)
val clubWebApi = ClubWebApi(clubServices)
val courtWebApi = CourtWebApi(courtServices)
val rentalWebApi = RentalWebApi(rentalServices)

fun main() {
    fun userRoutes(userWebApi: UserWebApi) =
        routes(
            "users/{uid}" bind GET to { req ->
                logRequest(req)
                userWebApi.getUserById(req)
            },
            "users" bind POST to { req ->
                logRequest(req)
                userWebApi.createUser(req)
            },
            "users" bind GET to { req ->
                logRequest(req)
                userWebApi.getAllUsers(req)
            },
            "login" bind POST to { req ->
                logRequest(req)
                userWebApi.Userlogin(req)
            },
        )

    fun clubRoutes(clubWebApi: ClubWebApi) =
        routes(
            "clubs" bind GET to { req ->
                logRequest(req)
                clubWebApi.getClubs(req)
            },
            "clubs/{cid}" bind GET to { req ->
                logRequest(req)
                clubWebApi.getClubById(req)
            },
            "clubsbyuser/{uid}" bind GET to { req ->
                logRequest(req)
                clubWebApi.listUserClubs(req)
            },
            "clubs" bind POST to { req ->
                logRequest(req)
                clubWebApi.createClub(req)
            },
        )

    fun courtRoutes(courtWebApi: CourtWebApi) =
        routes(
            "clubs/{cid}/courts" bind GET to { req ->
                logRequest(req)
                courtWebApi.getCourtsByClub(req)
            },
            "clubs/{cid}/courts" bind POST to { req ->
                logRequest(req)
                courtWebApi.createCourt(req)
            },
            "courts/{crid}" bind GET to { req ->
                logRequest(req)
                courtWebApi.getCourtById(req)
            },
        )

    fun rentalRoutes(rentalWebApi: RentalWebApi) =
        routes(
            "rentals/{rid}" bind GET to { req ->
                logRequest(req)
                rentalWebApi.getRentalById(req)
            },
            "rentals" bind GET to { req ->
                logRequest(req)
                rentalWebApi.getRentals(req)
            },
            "rentalsbyuser/{uid}" bind GET to { req ->
                logRequest(req)
                rentalWebApi.getUserRentalsById(req)
            },
            "clubs/{cid}/courts/{crid}/rentals" bind GET to { req ->
                logRequest(req)
                rentalWebApi.getRentalsByClubCourtDate(req)
            },
            "clubs/{cid}/courts/{crid}/availablehours" bind GET to { req ->
                logRequest(req)
                rentalWebApi.getAvailableHours(req)
            },
            "clubs/{cid}/courts/{crid}/rentals" bind POST to rentalWebApi::createRental,
            "rentals/{rid}" bind DELETE to { req ->
                logRequest(req)
                rentalWebApi.deleteRental(req)
            },
            "rentals/{rid}" bind PUT to { req ->
                logRequest(req)
                rentalWebApi.updateRental(req)
            },
        )

    val userRoutes = userRoutes(userWebApi)
    val clubRoutes = clubRoutes(clubWebApi)
    val courtRoutes = courtRoutes(courtWebApi)
    val rentalRoutes = rentalRoutes(rentalWebApi)

    val app =
        routes(
            userRoutes,
            clubRoutes,
            courtRoutes,
            rentalRoutes,
            "date" bind GET to ::getDate,
            singlePageApp(ResourceLoader.Directory("static-content")),
        )

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val jettyServer = app.asServer(Jetty(port)).start()
    logger.info("Server started listening on port $port")
    logger.info("Server started at http://localhost:$port")
    //logger.info("Server started at https://img-ls-2425-2-43-g07.onrender.com")

    Runtime.getRuntime().addShutdownHook(
        Thread {
            jettyServer.stop()
            logger.info("Server shutting down...")
        },
    )
}
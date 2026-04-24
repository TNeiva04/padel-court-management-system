
import pt.isel.ls.data.CourtRepository
import pt.isel.ls.data.DataMem.*
import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.http.Server.clubServices
import pt.isel.ls.http.Server.courtServices
import pt.isel.ls.http.Server.userRepo
import pt.isel.ls.http.Server.userServices
import pt.isel.ls.http.Services.ClubServices
import pt.isel.ls.http.Services.CourtServices
import pt.isel.ls.http.Services.UserServices
import pt.isel.ls.http.Web.API.models.EmailSerial
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.*


class CourtServicesTests {

    private val userRepo = InMemoryUserRepository()
    private val clubRepo = InMemoryClubRepository()
    private val courtRepo = InMemoryCourtRepository()

    private val userServices = UserServices(userRepo)
    private val clubServices = ClubServices(clubRepo, userRepo)
    private val courtServices = CourtServices(courtRepo)

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }


    @Test
    fun `createCourtService creates a court`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        assertTrue(courtId.crid > 0)
    }



    @Test
    fun `getCourtByIdService returns the crid of a court that exists`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password ="1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val createdCourt = courtServices.createCourtService("Court Of Chico", clubID)
        val court = courtServices.getCourtByIdService(createdCourt.crid)
        assertNotNull(court)
        assertEquals("Court Of Chico", court.name)
    }

    @Test
    fun `getCourtsByClubService returns a list of courts of a club`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password ="1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val createdCourt = courtServices.createCourtService("Court Of Chico", clubID)
        val courts = courtServices.getCourtsByClubService(clubID)
        assertEquals(createdCourt.crid, courts.courts.first().crid)
    }


}
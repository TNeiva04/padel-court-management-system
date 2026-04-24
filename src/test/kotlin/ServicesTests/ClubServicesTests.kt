
import pt.isel.ls.data.ClubRepository
import pt.isel.ls.data.DataMem.InMemoryClubRepository
import pt.isel.ls.data.DataMem.InMemoryUserRepository
import pt.isel.ls.data.DataMem.Users
import pt.isel.ls.data.UserRepository
import pt.isel.ls.http.Services.ClubServices
import pt.isel.ls.http.Services.UserServices
import kotlin.test.assertTrue
import kotlin.test.*


class ClubServicesTests {

    private val userRepo: UserRepository = InMemoryUserRepository()
    private val userServices = UserServices(userRepo)
    private val clubRepo: ClubRepository = InMemoryClubRepository()
    private val clubServices = ClubServices(clubRepo, userRepo)

    @Test
    fun `createClubService creates a club`() {
        val token = Users[0].token
        val club = clubServices.createClubService(("New Club"), token)
        assertTrue(club.cid > 0)
    }

    @Test
    fun `listUserClubsService returns the clubs of a user`() {
        val token = Users[0].token
        val uid = Users[0].uid
        val clubs = clubServices.listUserClubsService(token, uid)
        assertTrue(clubs.clubs.isNotEmpty())
    }

    @Test
    fun `listAllClubsService returns  all  clubs`() {
        val clubs = clubServices.listAllClubsService(name = null)
        assertTrue(clubs.clubs.isNotEmpty())
    }
}
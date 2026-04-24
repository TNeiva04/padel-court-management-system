import pt.isel.ls.data.DataMem.*
import pt.isel.ls.http.Web.API.models.EmailSerial
import kotlin.test.*
import pt.isel.ls.data.DataMem.InMemoryUserRepository
import pt.isel.ls.data.DataMem.InMemoryClubRepository
import pt.isel.ls.domain.Email

class ClubDataMemoryTests {

    private val userRepo: InMemoryUserRepository = InMemoryUserRepository()
    private val clubRepo: InMemoryClubRepository = InMemoryClubRepository()

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }


    @Test
    fun `createClub should create a new club`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name )
        assertEquals(1, club.cid)
    }

    @Test
    fun `getClubById should return the cid of a club that exists`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val foundClub = clubRepo.getClubID(Clubs[0].name)
        assertEquals(1, club.cid)
        assertEquals(club.cid, foundClub.cid)
    }

    @Test
    fun `getClubsByName should return the cid of a club that exists`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val foundClub = clubRepo.getClubsByName(Clubs[0].name)
        assertEquals(1, club.cid)
        assertEquals(club.cid, foundClub.clubs.first().cid)
    }


    @Test
    fun `getALLClubs should return a list of all the clubs`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val owner = userRepo.getUserById(user.uid)
        clubRepo.createClub("Club 1", Users[0].name)
        clubRepo.createClub("Club 2", Users[0].name)
        clubRepo.createClub("Club 3", Users[0].name)
        val allClubs = clubRepo.getAllClubs()
        assertEquals(3, allClubs.clubs.size)
    }
}
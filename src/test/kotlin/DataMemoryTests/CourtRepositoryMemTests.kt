package pt.isel.ls.phase_1

import pt.isel.ls.data.DataMem.*
import pt.isel.ls.http.Web.API.models.EmailSerial
import kotlin.test.*
import pt.isel.ls.data.DataMem.InMemoryUserRepository
import pt.isel.ls.data.DataMem.InMemoryClubRepository
import pt.isel.ls.data.DataMem.InMemoryCourtRepository
import pt.isel.ls.domain.Email

class CourtRepositoryMemTests {

    private val userRepo: InMemoryUserRepository = InMemoryUserRepository()
    private val clubRepo: InMemoryClubRepository = InMemoryClubRepository()
    private val courtRepo: InMemoryCourtRepository = InMemoryCourtRepository()

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }

    @Test
    fun `createCourt should create a new court`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val court = courtRepo.createCourt("Court 1", club.cid)
        assertEquals(1, court.crid)

    }

    @Test
    fun `getCourtById should return the crid of a court that exists`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val court = courtRepo.createCourt("Court 1", club.cid)
        val foundCourt = courtRepo.getCourtById(court.crid)
        assertEquals(court.crid, foundCourt.crid)
        assertEquals("Court 1", foundCourt.name)
        assertEquals(club.cid, foundCourt.club.cid)
    }

    @Test
    fun `getCourtsByClubService should return a list of all the courts of a club`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        courtRepo.createCourt("Court 1", club.cid)
        courtRepo.createCourt("Court 2", club.cid)
        courtRepo.createCourt("Court 3", club.cid)
        val allCourts = courtRepo.getCourtsByClub(club.cid)
        assertEquals(3, allCourts.courts.size)
    }

}
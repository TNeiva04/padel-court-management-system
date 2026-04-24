
import pt.isel.ls.data.DataMem.*
import pt.isel.ls.domain.*
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.DurationOutput
import pt.isel.ls.http.Web.API.models.EmailSerial
import pt.isel.ls.http.Web.API.models.HoursList
import pt.isel.ls.http.Web.API.stringToTimestamp2
import kotlin.test.*
import pt.isel.ls.data.DataMem.InMemoryUserRepository
import pt.isel.ls.data.DataMem.InMemoryClubRepository
import pt.isel.ls.data.DataMem.InMemoryCourtRepository
import pt.isel.ls.data.DataMem.InMemoryRentalRepository


class RentalDataMemTests {

    private val userRepo = InMemoryUserRepository()
    private val clubRepo = InMemoryClubRepository()
    private val courtRepo = InMemoryCourtRepository()
    private val rentalRepo = InMemoryRentalRepository()

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }


    @Test
    fun `createRental should create a new rental`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Clube 1", Users[0].name)
        val court = courtRepo.createCourt("Quadra 1", club.cid)
        val rentalDate = stringToTimestamp2("2023-03-15T14:30:45")
        val rental = rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate,
            2,
            user.uid
        )
        assertEquals(1, rental.rid)
    }

    @Test
    fun `getRentalById should return information about a rental`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Clube 1", Users[0].name)
        val court = courtRepo.createCourt("Quadra 1", club.cid)
        val rentalDate = stringToTimestamp2("2023-03-15T14:30:45")
        val rental = rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate,
            2,
            user.uid
        )
        val foundRental = rentalRepo.getRentalById(rental.rid)
        assertEquals(rental.rid, foundRental.rid)
        assertEquals(rentalDate, foundRental.datein)
        assertEquals(2, foundRental.duration)
        assertEquals(user.uid, foundRental.uid.uid)
        assertEquals(court.crid, foundRental.crid.crid)
    }

    @Test
    fun `getRentalsByUser should return all rentals of a user`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Clube 1", Users[0].name)
        val court = courtRepo.createCourt("Quadra 1", club.cid)
        val rentalDate1 = stringToTimestamp2("2023-03-15T14:30:45")
        val rentalDate2 = stringToTimestamp2("2023-03-16T14:30:45")
        val rentalDate3 = stringToTimestamp2("2023-03-17T14:30:45")
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate1,
            2,
            user.uid
        )
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate2,
            3,
            user.uid
        )
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate3,
            1,
            user.uid
        )
        val allRentals = rentalRepo.getRentalsByUser(user.uid)
        assertEquals(3, allRentals.size)
    }

    @Test
    fun `getAvailableHours should return all available hours of a court in a specific date`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Clube 1", Users[0].name)
        val court = courtRepo.createCourt("Quadra 1", club.cid)
        val rentalDate = stringToTimestamp2("2023-03-15T01:00:00")
        val rentalHours1 = stringToTimestamp2("2023-03-15T10:00:00")
        val rentalHours2 = stringToTimestamp2("2023-03-15T14:00:00")
        val rentalHours3 = stringToTimestamp2("2023-03-15T18:00:00")

        // Criando reservas corretamente com o formato "yyyy-MM-dd HH:mm:ss"
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalHours1,
            2,
            user.uid
        )
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalHours2,
            3,
            user.uid
        )
        rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalHours3,
            1,
            user.uid
        )

        val allHours = rentalRepo.getAvailableHours(club.cid, court.crid, rentalDate)

        val expectedAvailableHours = HoursList(
            mutableListOf(
                "2023-03-15 00:00",
                "2023-03-15 01:00",
                "2023-03-15 02:00",
                "2023-03-15 03:00",
                "2023-03-15 04:00",
                "2023-03-15 05:00",
                "2023-03-15 06:00",
                "2023-03-15 07:00",
                "2023-03-15 08:00",
                "2023-03-15 09:00",
                "2023-03-15 12:00",
                "2023-03-15 13:00",
                "2023-03-15 17:00",
                "2023-03-15 19:00",
                "2023-03-15 20:00",
                "2023-03-15 21:00",
                "2023-03-15 22:00",
                "2023-03-15 23:00"
            )
        )

        assertEquals(expectedAvailableHours, allHours)
    }


    @Test
    fun `deleteRental should delete a rental with a certain ID`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val court = courtRepo.createCourt("Court 1", club.cid)
        val rentalDate = stringToTimestamp2("2023-03-15T09:00:00")
        val rental = rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate,
            2,
            user.uid
        )
        val deleted = rentalRepo.deleteRental(rental.rid, user.token)
        assertTrue(deleted)
        assertFailsWith<InternalError.RentalNotFound> {
            rentalRepo.getRentalById(rental.rid)
        }
    }

    @Test
    fun `updateRental should update date and duration of a certain rental`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val club = clubRepo.createClub("Club 1", Users[0].name)
        val court = courtRepo.createCourt("Court 1", club.cid)
        val rentalDate = stringToTimestamp2("2023-03-15T09:00:00")
        val updatedRentalDate = stringToTimestamp2("2023-03-16T09:00:00")
        val rental = rentalRepo.createRental(
            club.cid,
            court.crid,
            rentalDate,
            2,
            user.uid
        )
        val updatedRental = rentalRepo.updateRental(
            rental.rid,
            updatedRentalDate,
            Duration(3).duration
        )
        assertEquals(rental.rid, updatedRental.rid)
        assertEquals(updatedRentalDate, updatedRental.datein)
        assertEquals(3, updatedRental.duration)
    }
}

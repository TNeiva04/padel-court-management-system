
import pt.isel.ls.data.DataMem.*
import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.http.Services.ClubServices
import pt.isel.ls.http.Services.CourtServices
import pt.isel.ls.http.Services.RentalServices
import pt.isel.ls.http.Services.UserServices
import pt.isel.ls.http.Web.API.models.EmailSerial
import pt.isel.ls.http.Web.API.stringToTimestamp2
import java.sql.Timestamp
import kotlin.test.*

class RentalServicesTests {
    private val userRepo = InMemoryUserRepository()
    private val clubRepo = InMemoryClubRepository()
    private val courtRepo = InMemoryCourtRepository()

    private val userServices = UserServices(userRepo)
    private val clubServices = ClubServices(clubRepo, userRepo)
    private val courtServices = CourtServices(courtRepo)

    private val rentalRepo = InMemoryRentalRepository()
    private val rentalServices = RentalServices(rentalRepo, userRepo)

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }

    @Test
    fun `createRentalService creates a rental`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val date = "2023-03-15T09:00:00"
        val rental = rentalServices.createRentalService(clubID, courtId.crid, date, 2, token)

        assertTrue(rental.rid > 0)
    }

    @Test
    fun `getRentalByIdService returns the rid if a rental exists`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val date = "2023-03-15T09:00:00"

        // Cria um rental primeiro
        val createdRental = rentalServices.createRentalService(clubID, courtId.crid, date, 2, token)

        // Vai buscá-lo pelo ID
        val rental = rentalServices.getRentalByIdService(createdRental.rid)

        assertNotNull(rental)
        assertEquals(createdRental.rid, rental.rid)
    }


    @Test
    fun `getRentalsByClubCourtDateService returns a list of rentals for a club, court and date`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val date = "2023-03-15T09:00:00"

        // Cria um rental para a data, clube e campo pretendido
        rentalServices.createRentalService(clubID, courtId.crid, date, 2, token)

        // Obtém as reservas para esse clube, campo e data
        val rentals = rentalServices.getRentalsByClubCourtDateService(clubID, courtId.crid, date)


        // Verificar se as reservas existem
        assertNotNull(rentals)

        // Verificar se o número de reservas é o esperado
        assertEquals(1, rentals.size)
    }


    @Test
    fun `getRentalsByUserService returns a list of rentals for a user`() {

        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")


        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val date2 = "2023-03-15T09:00:00"
        val date = stringToTimestamp2("2023-03-15T09:00:00")
        rentalServices.createRentalService(clubID, courtId.crid, date2, 2, token)

        val rentals = rentalServices.getRentalsByUserService(token)


        assertNotNull(rentals)
        assertEquals(1, rentals.size)

        val rental = rentals[0]
        assertEquals(courtId.crid, rental.crid.crid)
        assertEquals(date, rental.datein)
    }


    @Test
    fun `getAvailableHoursService returns a list with the available hours`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)

        val date = "2023-03-15T00:00:00"  // Apenas a data (sem hora), se for esse o esperado pelo serviço
        val availableHours = rentalServices.getAvailableHoursService(clubID, courtId.crid, date)

        assertNotNull(availableHours)

        // Este número depende da tua lógica de horários disponíveis (ex: 8h às 22h → 14 slots)
        val expectedAvailableSlots =  24
        assertEquals(expectedAvailableSlots, availableHours.hours.size)
    }


    @Test
    fun `deleteRentalService deletes a rental`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val date = "2023-03-15T09:00:00"
        val rentalId = rentalServices.createRentalService(clubID, courtId.crid, date, 2, token)
        val result = rentalServices.deleteRentalService(rentalId.rid, token)

        assertTrue(result)
    }

    @Test
    fun `updateRentalService updates a rental`() {
        val (uid, token) = userServices.createUserService("New User", Email("newuser@email.com"), password = "1234")
        val clubID = clubServices.createClubService("New Club", token).cid
        val courtId = courtServices.createCourtService("Court Of Chico", clubID)
        val rental = rentalServices.createRentalService(clubID, courtId.crid, "2023-03-15T09:00:00", 2, token)
        val rentalId = rental.rid

        val updatedRental = rentalServices.updateRentalService(rentalId, "2023-03-15T11:00:00", 3, token)

        assertNotNull(updatedRental)
        assertEquals(3, updatedRental.duration)
    }

}
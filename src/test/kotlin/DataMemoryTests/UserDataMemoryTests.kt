import pt.isel.ls.data.DataMem.*
import pt.isel.ls.data.DataMem.InMemoryUserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.http.Web.API.models.EmailSerial
import kotlin.test.*
import kotlin.test.assertEquals

class UserDataMemoryTests {
    private val userRepo = InMemoryUserRepository()

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }

    @Test
    fun `createUser should create a new user`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val user2 = userRepo.createUser("Maria Antunes", Email("mariaantunes@email.com"), password = "1234")
        assertEquals(1, user.uid)
        assertEquals(2, user2.uid)
    }

    @Test
    fun `getUserById should return the uid of a user that exists`() {
        val user = userRepo.createUser("Tiago Neiva", Email("tiago@email.com"), password = "1234")
        val foundUser = userRepo.getUserById(user.uid)
        assertEquals(user.uid, foundUser.uid)
        assertEquals(user.token, foundUser.token)
    }

    @Test
    fun `getAllUsers should return all users`() {
        userRepo.createUser("João Silva", Email("joaosilva@email.com"), password = "1234")
        userRepo.createUser("Maria Antunes", Email("mariaantunes@email.com"), password = "1234")
        val users = userRepo.getAllUsers()
        assertEquals(2, users.size)
        assertEquals("João Silva", users[0].name)
        assertEquals("Maria Antunes", users[1].name)
    }
}

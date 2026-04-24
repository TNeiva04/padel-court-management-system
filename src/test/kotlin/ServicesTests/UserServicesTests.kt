
import pt.isel.ls.data.DataMem.*
import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.*
import pt.isel.ls.http.Services.UserServices
import pt.isel.ls.http.Web.API.models.EmailSerial


class UserServicesTests {

    private val userRepo: UserRepository = InMemoryUserRepository()
    private val userServices = UserServices(userRepo)

    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
    }


    @Test
    fun `createUserService creates a user`() {
        val (uid, token) = userServices.createUserService("New User", Email("new@email.com"), password = "1234")
        assertTrue(uid > 0)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `findUserService returns the uid of an existing user`() {
        val (uid, _) = userServices.createUserService("New User", Email("new3@email.com"), password = "1234")
        val user = userServices.findUserService(uid)
        assertEquals("New User", user.name)
    }

    @Test
    fun `getAllUsersService returns all users`() {
        userServices.createUserService("New User", Email("new@email.com"), password = "1234")
        userServices.createUserService("New User2", Email("new2@email.com"), password = "1234")
        val users = userServices.getAllUsersService()
        assertEquals(2, users.size)
        assertEquals("New User", users[0].name)
        assertEquals("New User2", users[1].name)
    }
}
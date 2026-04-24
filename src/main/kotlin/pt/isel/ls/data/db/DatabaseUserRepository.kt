package pt.isel.ls.data.db

import pt.isel.ls.data.UserRepository
import pt.isel.ls.domain.Email
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Server.dataSource
import pt.isel.ls.http.Web.API.models.*
import java.util.*

class DatabaseUserRepository(
    dataSource: Any?,
) : UserRepository.UserRepositoryDB {
    override fun createUser(
        name: String,
        email: Email,
        password: String,
    ): UserOutputCreateUser {
        val token = UUID.randomUUID().toString()

        dataSource.connection.use { conn ->
            conn.autoCommit = false

            try {
                val sql = "INSERT INTO user_table(name, email, token, password) VALUES (?, ?, ?, ?) RETURNING uid"
                conn.prepareStatement(sql).use { stm ->
                    stm.setString(1, name)
                    stm.setString(2, email.email)
                    stm.setString(3, token)
                    stm.setString(4, password)

                    stm.executeQuery().use { rs ->
                        if (rs.next()) {
                            val userId = rs.getInt("uid")
                            conn.commit()
                            return UserOutputCreateUser(userId, token)
                        } else {
                            conn.rollback()
                            throw RuntimeException("Creating user failed, no ID obtained.")
                        }
                    }
                }
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }

    override fun checkEmailNotExists(email: Email): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT uid FROM user_table WHERE email = ?").use { stmt ->
                stmt.setString(1, email.email)
                val rs = stmt.executeQuery()
                return !rs.next()
            }
        }
    }

    override fun getUserById(uid: Int): UserOutputGetDetails {
        dataSource.connection.use { conn ->
            val sql = "SELECT name, email, token FROM user_table WHERE uid = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, uid)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputGetDetails(
                            uid,
                            rs.getString("name"),
                            EmailSerial(rs.getString("email")),
                            rs.getString("token"),
                        )
                    } else {
                        // throw RuntimeException("User ID not found.")
                        throw throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun getUserID(name: String): UserOutputWithID {
        dataSource.connection.use { conn ->
            val sql = "SELECT uid FROM user_table WHERE name = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, name)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputWithID(rs.getInt("uid"))
                    } else {
                        println("User not found.")
                        // throw RuntimeException("User not found.")
                        throw throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun dbGetUserName(uid: Int): UserOutputName {
        dataSource.connection.use { conn ->
            val sql = "SELECT name FROM user_table WHERE uid = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, uid)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputName(rs.getString("name"))
                    } else {
                        throw throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun dbGetUserDetailsByID(uid: Int): UserOutputGetDetails {
        dataSource.connection.use { conn ->
            val sql = "SELECT name, email,token FROM user_table WHERE uid = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, uid)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputGetDetails(
                            uid,
                            rs.getString("name"),
                            EmailSerial(rs.getString("email")),
                            rs.getString("token"),
                        )
                    } else {
                        throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun getUserByToken(token: String): UserOutputWithToken {
        dataSource.connection.use { conn ->
            val sql = "SELECT uid, name FROM user_table WHERE token = ?"

            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, token)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputWithToken(rs.getInt("uid"), rs.getString("name"))
                    } else {
                        throw throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun tokenVerify(
        token: String,
        uid: Int,
    ): Boolean {
        dataSource.connection.use { conn ->
            val sql = "SELECT token FROM user_table WHERE token = ?"

            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, token)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        rs.getString("token") == token
                    } else {
                        // throw RuntimeException("User ID not found.")
                        throw InternalError.UserNotFound
                    }
                }
            }
        }
    }

    override fun getAllUsers(): List<UserOutputGetDetails> {
        dataSource.connection.use { conn ->
            val sql = "SELECT uid, name, email, token FROM user_table"
            conn.prepareStatement(sql).use { stm ->
                stm.executeQuery().use { rs ->
                    val users = mutableListOf<UserOutputGetDetails>()
                    while (rs.next()) {
                        users.add(
                            UserOutputGetDetails(
                                rs.getInt("uid"),
                                rs.getString("name"),
                                EmailSerial(rs.getString("email")),
                                rs.getString("token"),
                            ),
                        )
                    }
                    return users
                }
            }
        }
    }

    override fun tokenVerify_forCourt(
        token: String,
        clubId: Int,
    ): Boolean {
        dataSource.connection.use { conn ->
            // Obter uid do user pelo token
            val getUserSql = "SELECT uid FROM user_table WHERE token = ?"
            val uid =
                conn.prepareStatement(getUserSql).use { stm ->
                    stm.setString(1, token)
                    stm.executeQuery().use { rs ->
                        if (rs.next()) {
                            rs.getInt("uid")
                        } else {
                            throw InternalError.UserNotFound
                        }
                    }
                }

            // Verificar se o owner do clube é o uid obtido
            val getClubOwnerSql = "SELECT owner FROM club WHERE cid = ?"
            return conn.prepareStatement(getClubOwnerSql).use { stm ->
                stm.setInt(1, clubId)
                stm.executeQuery().use { rs ->
                    if (rs.next()) {
                        val ownerId = rs.getInt("owner")
                        ownerId == uid
                    } else {
                        false
                    }
                }
            }
        }
    }

    override fun tokenVerify_forRentalUpdate(
        user: Int,
        rid: Int,
    ): Boolean {
        dataSource.connection.use { conn ->
            val sql = "SELECT uid FROM rental WHERE rid = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, rid)
                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        val rentalUserId = rs.getInt("uid")
                        rentalUserId == user
                    } else {
                        // rental não encontrado
                        false
                    }
                }
            }
        }
    }

    override fun loginUser(email: String, password: String): UserOutputCreateUser {
        dataSource.connection.use { conn ->
            val sql = "SELECT uid, token FROM user_table WHERE email = ? AND password = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, email)
                stm.setString(2, password)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        UserOutputCreateUser(
                            uid = rs.getInt("uid"),
                            token = rs.getString("token"),
                        )
                    } else {
                        throw InternalError.InvalidCredentials(
                            "Invalid email or password.",
                        )
                    }
                }
            }
        }
    }
}

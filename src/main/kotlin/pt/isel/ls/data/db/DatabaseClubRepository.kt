package pt.isel.ls.data.db

import pt.isel.ls.data.ClubRepository
import pt.isel.ls.data.*
import pt.isel.ls.http.Server.*
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*

class DatabaseClubRepository(dataSource: Any?) : ClubRepository.ClubRepositoryBD {

    override fun createClub(name: String, owner: String): ClubOutput {
        dataSource.connection.use { conn ->
            conn.autoCommit = false

            try {
                val idOwner = DatabaseUserRepository(dataSource).getUserID(owner).uid
                val sql = "INSERT INTO club(name, owner) VALUES (?, ?) RETURNING cid"
                conn.prepareStatement(sql).use { stm ->
                    stm.setString(1, name)
                    stm.setInt(2, idOwner)

                    stm.executeQuery().use { rs ->
                        if (rs.next()) {
                            val cid = rs.getInt("cid")
                            conn.commit()
                            return ClubOutput(cid)
                        } else {
                            throw RuntimeException("Creating club failed, no ID obtained.")
                        }
                    }
                }
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }


    override fun getClubID(club: String): ClubOutput {
        dataSource.connection.use { conn ->
            val sql = "SELECT cid FROM club WHERE name = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, club)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        ClubOutput(rs.getInt("cid"))
                    } else {


                        throw InternalError.ClubNotFound("Club with name ${club} not found.")

                    }
                }
            }

        }
    }

    override fun getAllClubs(): ClubList {
        val clubs = mutableListOf<ClubOutputWithIDCompleted>()

        dataSource.connection.use { conn ->
            val sql = """
            SELECT c.cid, c.name, u.uid, u.name AS owner_name, u.email, u.token 
            FROM Club c
            JOIN User_Table u ON c.owner = u.uid
        """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.executeQuery().use { rs ->
                    while (rs.next()) {
                        val owner = UserOutputGetDetails(
                            uid = rs.getInt("uid"),
                            name = rs.getString("owner_name"),
                            email = EmailSerial(rs.getString("email")),
                        )

                        clubs.add(
                            ClubOutputWithIDCompleted(
                                cid = rs.getInt("cid"),
                                name = rs.getString("name"),
                                owner = owner
                            )
                        )
                    }
                }
            }
        }

        return ClubList(clubs)
    }

    override fun getClubsByName(name2: String): ClubList {
        val clubs = mutableListOf<ClubOutputWithIDCompleted>()

        val sql = """
        SELECT c.cid, c.name, u.uid AS owner_uid, u.name AS owner_name, u.email
        FROM Club c
        JOIN User_Table u ON c.owner = u.uid
        WHERE LOWER(c.name) LIKE LOWER(?)
    """.trimIndent()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, "%$name2%")
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    clubs.add(
                        ClubOutputWithIDCompleted(
                            cid = rs.getInt("cid"),
                            name = rs.getString("name"),
                            owner = UserOutputGetDetails(
                                uid = rs.getInt("owner_uid"),
                                name = rs.getString("owner_name"),
                                email = EmailSerial(rs.getString("email")),
                            )
                        )
                    )
                }
            }
        }

        return ClubList(clubs)
    }


    override fun getClubsOfaUser(userId: Int): ClubList {
        val clubs = mutableListOf<ClubOutputWithIDCompleted>()

        dataSource.connection.use { conn ->
            val sql = """
            SELECT c.cid, c.name, u.uid, u.name AS owner_name, u.email, u.token
            FROM Club c
            JOIN User_Table u ON c.owner = u.uid
            WHERE u.uid = ?
        """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, userId)
                stm.executeQuery().use { rs ->
                    while (rs.next()) {
                        val owner = UserOutputGetDetails(
                            uid = rs.getInt("uid"),
                            name = rs.getString("owner_name"),
                            email = EmailSerial(rs.getString("email"))
                        )

                        clubs.add(
                            ClubOutputWithIDCompleted(
                                cid = rs.getInt("cid"),
                                name = rs.getString("name"),
                                owner = owner
                            )
                        )
                    }
                }
            }
        }

        return ClubList(clubs)
    }


    override fun getClubDetails(club: Int): ClubOutputWithIDCompleted {
        dataSource.connection.use { conn ->
            val sql = """
            SELECT c.cid, c.name, u.uid, u.name AS owner_name, u.email, u.token 
            FROM Club c
            JOIN User_Table u ON c.owner = u.uid
            WHERE c.cid = ?
        """.trimIndent()
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, club)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        ClubOutputWithIDCompleted(
                            rs.getInt("cid"),
                            rs.getString("name"),
                            UserOutputGetDetails(
                                uid = rs.getInt("uid"),
                                name = rs.getString("owner_name"),
                                email = EmailSerial(rs.getString("email"))
                            )
                        )
                    } else {
                        //throw RuntimeException("Club not found.")
                        throw InternalError.ClubNotFound("Club with id ${club} not found.")

                    }
                }
            }
        }
    }


    override fun String.isNotInDB(): Boolean {
        dataSource.connection.use { conn ->
            val sql = "SELECT 1 FROM club WHERE name = ? LIMIT 1"
            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, this)
                stm.executeQuery().use { rs ->
                    return !rs.next()
                }
            }
        }
    }

}

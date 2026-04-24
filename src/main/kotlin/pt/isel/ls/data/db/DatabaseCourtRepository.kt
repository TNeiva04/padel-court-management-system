package pt.isel.ls.data.db

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.data.CourtRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Server.*
import pt.isel.ls.http.Web.API.models.*
import java.sql.Statement

class DatabaseCourtRepository(dataSource: PGSimpleDataSource) : CourtRepository {
    override fun createCourt(name: String, cid: Int): CourtOutputWithID {
        dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {
                // Verifica se o club existe
                conn.prepareStatement("SELECT 1 FROM Club WHERE cid = ?").use { checkStm ->
                    checkStm.setInt(1, cid)
                    checkStm.executeQuery().use { rs ->
                        if (!rs.next()) {
                            throw InternalError.ClubNotFound("Club with id $cid not found.")
                            // throw IllegalArgumentException("Club with ID $cid not found.")
                        }
                    }
                }
                //Verifica se já existe um Court com o mesmo nome para o club
                conn.prepareStatement("SELECT 1 FROM Court WHERE name = ? AND cid = ?").use { checkCourtStm ->
                    checkCourtStm.setString(1, name)
                    checkCourtStm.setInt(2, cid)
                    checkCourtStm.executeQuery().use { rs ->
                        if (rs.next()) {
                            throw InternalError.CourtAlreadyExists("Court with name '$name' already exists for club with id $cid.")
                        }
                    }
                }

                conn.prepareStatement(
                    "INSERT INTO Court(name, cid) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { stm ->
                    stm.setString(1, name)
                    stm.setInt(2, cid)

                    val affectedRows = stm.executeUpdate()
                    if (affectedRows == 0) {
                        throw RuntimeException("Creating court failed, no rows affected.")
                    }


                    stm.generatedKeys.use { rs ->
                        if (rs.next()) {
                            val courtId = rs.getInt(1)
                            conn.commit()
                            return CourtOutputWithID(courtId)
                        } else {
                            throw RuntimeException("Creating court failed, no ID obtained.")
                        }
                    }
                }
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }


    override fun getCourtId(name: String): CourtOutputWithID {
        dataSource.getConnection().use { conn ->
            val sql = "SELECT crid FROM Court WHERE name = ?"
            conn.prepareStatement(sql).use { stm ->
                stm.setString(1, name)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {
                        CourtOutputWithID(rs.getInt("crid"))
                    } else {
                        // throw RuntimeException("Court not found.")
                        throw InternalError.ClubNotFound(" with $name.")
                    }
                }
            }
        }
    }


    override fun getCourtById(crid: Int): CourtOutputCompleted {
        dataSource.getConnection().use { conn ->

            val sql = """
            SELECT c.crid, c.name, c.cid, cl.name AS club_name, u.uid, u.name AS user_name, u.email, u.token
            FROM Court c
            JOIN Club cl ON c.cid = cl.cid
            JOIN User_Table u ON cl.owner = u.uid
            WHERE c.crid = ?
        """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, crid)

                stm.executeQuery().use { rs ->
                    return if (rs.next()) {

                        CourtOutputCompleted(
                            crid = rs.getInt("crid"),
                            name = rs.getString("name"),
                            club = ClubOutputWithIDCompleted(
                                cid = rs.getInt("cid"),
                                name = rs.getString("club_name"),
                                owner = UserOutputGetDetails(
                                    uid = rs.getInt("uid"),
                                    name = rs.getString("user_name"),
                                    email = EmailSerial(rs.getString("email"))
                                )
                            )
                        )
                    } else {
                        // throw RuntimeException("Court not found.")
                        throw InternalError.ClubNotFound(" $crid.")
                    }
                }
            }
        }
    }


    override fun getCourtsByClub(cid: Int): CourtList {
        dataSource.connection.use { conn ->

            val sql = """
            SELECT cr.crid, cr.name AS courtName, c.cid, c.name AS clubName, u.uid, u.name AS userName, u.email, u.token
            FROM Court cr
            JOIN Club c ON cr.cid = c.cid
            JOIN User_Table u ON c.owner = u.uid
            WHERE cr.cid = ?
        """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, cid)

                stm.executeQuery().use { rs ->
                    val courts = mutableListOf<CourtOutputCompleted>()
                    while (rs.next()) {
                        courts.add(
                            CourtOutputCompleted(
                                crid = rs.getInt("crid"),
                                name = rs.getString("courtName"),
                                club = ClubOutputWithIDCompleted(
                                    cid = rs.getInt("cid"),
                                    name = rs.getString("clubName"),
                                    owner = UserOutputGetDetails(
                                        uid = rs.getInt("uid"),
                                        name = rs.getString("userName"),
                                        email = EmailSerial(rs.getString("email"))
                                    )
                                )
                            )
                        )
                    }
                    return CourtList(courts)
                }
            }
        }
    }
}


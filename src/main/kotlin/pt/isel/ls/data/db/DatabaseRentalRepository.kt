package pt.isel.ls.data.db

import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ls.data.RentalRepository
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Server.*
import pt.isel.ls.http.Web.API.models.*
import java.sql.Statement
import java.sql.Timestamp

class DatabaseRentalRepository(
    dataSource: PGSimpleDataSource,
) : RentalRepository {

    override fun createRental(
        clubID: Int,
        crid: Int,
        datein: Timestamp,
        duration: Int,
        uid: Int,
    ): OutputRental {
        dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {
                var rid = OutputRental(0)

                // Verifica se o court pertence ao clube
                val courtClubId = conn.prepareStatement("SELECT cid FROM Court WHERE crid = ?").use { stm ->
                    stm.setInt(1, crid)
                    stm.executeQuery().use { rs -> if (rs.next()) rs.getInt("cid") else null }
                } ?: throw InternalError.CourtNotFound("Court $crid not found")

                if (courtClubId != clubID) {
                    throw InternalError.InvalidParameter("Court $crid does not belong to club $clubID")
                }

                val startDateTime = datein.toLocalDateTime()
                val endOfDay = startDateTime.toLocalDate().atTime(23, 59, 59) // mais seguro que LocalTime.MAX

                var remainingHours = duration
                var currentStart = startDateTime
                val rentals = mutableListOf<OutputRental>()

                while (remainingHours > 0) {
                    var currentEnd = currentStart.plusHours(remainingHours.toLong())
                    if (currentEnd.isAfter(endOfDay)) {
                        currentEnd = endOfDay
                    }

                    val durationMinutes = java.time.Duration.between(currentStart, currentEnd).toMinutes()
                    val currentDuration = (durationMinutes / 60).coerceAtLeast(1) // garante mínimo de 1 hora

                    // Evita criar uma reserva inválida
                    if (currentDuration <= 0) {
                        throw IllegalArgumentException("Invalid rental duration: less than 1 hour")
                    }

                    val sql = """
                    INSERT INTO Rental (crid, uid, rental_date, rental_enddate, duration_hours)
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stm ->
                        stm.setInt(1, crid)
                        stm.setInt(2, uid)
                        stm.setTimestamp(3, Timestamp.valueOf(currentStart))
                        stm.setTimestamp(4, Timestamp.valueOf(currentEnd))
                        stm.setInt(5, currentDuration.toInt())

                        val affectedRows = stm.executeUpdate()
                        if (affectedRows == 0) throw RuntimeException("Creating rental failed, no rows affected.")

                        stm.generatedKeys.use { rs ->
                            if (rs.next()) {
                                rid = OutputRental(rs.getInt(1))
                                rentals.add(rid)
                            } else {
                                throw RuntimeException("Creating rental failed, no ID obtained.")
                            }
                        }
                    }

                    remainingHours -= currentDuration.toInt()
                    currentStart = currentEnd.plusSeconds(1)
                }

                conn.commit()
                return rid
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }


    override fun getRentalById(rid: Int): RentalOutput {
        dataSource.connection.use { conn ->
            val sql =
                """
                SELECT r.rid, r.rental_date, r.rental_enddate, r.duration_hours, r.crid, u.uid AS user_uid, u.name AS user_name, 
                       u.email AS user_email, u.token AS user_token, 
                       c.cid AS club_cid, c.name AS club_name, c.owner AS club_owner_id, 
                       cr.crid AS court_crid, cr.name AS court_name
                FROM Rental r
                JOIN User_Table u ON r.uid = u.uid
                JOIN Court cr ON r.crid = cr.crid
                JOIN Club c ON cr.cid = c.cid
                WHERE r.rid = ?
                """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, rid)

                stm.executeQuery().use { rs ->
                    if (!rs.next()) {
                        throw InternalError.RentalNotFound("Rental with ID $rid not found.")
                    } else {
                        return RentalOutput(
                            rid = rs.getInt("rid"),
                            crid =
                                CourtOutputCompleted(
                                    crid = rs.getInt("court_crid"),
                                    name = rs.getString("court_name"),
                                    club =
                                        ClubOutputWithIDCompleted(
                                            cid = rs.getInt("club_cid"),
                                            name = rs.getString("club_name"),
                                            owner =
                                                UserOutputGetDetails(
                                                    uid = rs.getInt("club_owner_id"),
                                                    name = rs.getString("user_name"),
                                                    email = EmailSerial(rs.getString("user_email")),
                                                ),
                                        ),
                                ),
                            uid =
                                UserDetails(
                                    uid = rs.getInt("user_uid"),
                                    name = rs.getString("user_name"),
                                    email = EmailSerial(rs.getString("user_email")),
                                    password = "PRIVATE",
                                    token = "PRIVATE",
                                ),
                            datein = rs.getTimestamp("rental_date"),
                            datefim = rs.getTimestamp("rental_enddate"),
                            duration = rs.getInt("duration_hours"),
                        )
                    }
                }
            }
        }
    }

    override fun getRentalsByClubCourtDate(
        cid: Int,
        crid: Int,
        date: Timestamp,
    ): List<RentalOutput> {
        dataSource.connection.use { conn ->
            val sql =
                """
                SELECT r.rid, r.rental_date, r.rental_enddate, r.duration_hours, r.crid, u.uid AS user_uid, u.name AS user_name, 
                   u.email AS user_email, u.token AS user_token, 
                   c.cid AS club_cid, c.name AS club_name, c.owner AS club_owner_id, 
                   cr.crid AS court_crid, cr.name AS court_name
                FROM Rental r
                INNER JOIN User_Table u ON r.uid = u.uid
                INNER JOIN Court cr ON r.crid = cr.crid
                INNER JOIN Club c ON cr.cid = c.cid
                WHERE c.cid = ? AND r.crid = ? AND DATE(r.rental_date) = DATE(?)
                """.trimIndent()
            val rentals = mutableListOf<RentalOutput>()
            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, cid)
                stm.setInt(2, crid)
                stm.setTimestamp(3, date)

                stm.executeQuery().use { rs ->
                    while (rs.next()) {
                        rentals.add(
                            RentalOutput(
                                rid = rs.getInt("rid"),
                                crid =
                                    CourtOutputCompleted(
                                        crid = rs.getInt("court_crid"),
                                        name = rs.getString("court_name"),
                                        club =
                                            ClubOutputWithIDCompleted(
                                                cid = rs.getInt("club_cid"),
                                                name = rs.getString("club_name"),
                                                owner =
                                                    UserOutputGetDetails(
                                                        uid = rs.getInt("club_owner_id"),
                                                        name = rs.getString("user_name"),
                                                        email = EmailSerial(rs.getString("user_email")),
                                                    ),
                                            ),
                                    ),
                                uid =
                                    UserDetails(
                                        uid = rs.getInt("user_uid"),
                                        name = rs.getString("user_name"),
                                        email = EmailSerial(rs.getString("user_email")),
                                        password = "PRIVATE",
                                        token = "PRIVATE",
                                    ),
                                datein = rs.getTimestamp("rental_date"),
                                datefim = rs.getTimestamp("rental_enddate"),
                                duration = rs.getInt("duration_hours"),
                            ),
                        )
                    }
                    return rentals
                }
            }
        }
    }

    override fun getRentalsByUser(uid: Int): List<RentalOutput> {
        dataSource.connection.use { conn ->

            val sql =
                """
                SELECT r.rid, r.rental_date, r.rental_enddate, r.duration_hours, r.crid, 
                       u.uid AS user_uid, u.name AS user_name, u.email AS user_email, u.token AS user_token, 
                       c.cid AS club_cid, c.name AS club_name, c.owner AS club_owner_id, 
                       cr.crid AS court_crid, cr.name AS court_name
                FROM Rental r
                JOIN User_Table u ON r.uid = u.uid
                JOIN Court cr ON r.crid = cr.crid
                JOIN Club c ON cr.cid = c.cid
                WHERE u.uid = ?
                """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, uid)

                stm.executeQuery().use { rs ->
                    val rentals = mutableListOf<RentalOutput>()
                    while (rs.next()) {
                        // Use duration_hours instead of 'time'
                        val duration = DurationOutput(rs.getInt("duration_hours"))

                        rentals.add(
                            RentalOutput(
                                rid = rs.getInt("rid"),
                                crid =
                                    CourtOutputCompleted(
                                        crid = rs.getInt("court_crid"),
                                        name = rs.getString("court_name"),
                                        club =
                                            ClubOutputWithIDCompleted(
                                                cid = rs.getInt("club_cid"),
                                                name = rs.getString("club_name"),
                                                owner =
                                                    UserOutputGetDetails(
                                                        uid = rs.getInt("club_owner_id"),
                                                        name = rs.getString("user_name"),
                                                        email = EmailSerial(rs.getString("user_email")),
                                                    ),
                                            ),
                                    ),
                                uid =
                                    UserDetails(
                                        uid = rs.getInt("user_uid"),
                                        name = rs.getString("user_name"),
                                        email = EmailSerial(rs.getString("user_email")),
                                        password = "PRIVATE",
                                        token = "PRIVATE",
                                    ),
                                datein = rs.getTimestamp("rental_date"),
                                datefim = rs.getTimestamp("rental_enddate"),
                                duration = rs.getInt("duration_hours"),
                            ),
                        )
                    }
                    return rentals
                }
            }
        }
    }

    override fun getRentals(): List<RentalOutput> {
        dataSource.connection.use { conn ->
            val sql =
                """
                SELECT r.rid, r.rental_date, r.rental_enddate, r.duration_hours, r.crid, 
                       u.uid AS user_uid, u.name AS user_name, u.email AS user_email, u.token AS user_token, 
                       c.cid AS club_cid, c.name AS club_name, c.owner AS club_owner_id, 
                       cr.crid AS court_crid, cr.name AS court_name
                FROM Rental r
                JOIN User_Table u ON r.uid = u.uid
                JOIN Court cr ON r.crid = cr.crid
                JOIN Club c ON cr.cid = c.cid
                """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.executeQuery().use { rs ->
                    val rentals = mutableListOf<RentalOutput>()
                    while (rs.next()) {
                        rentals.add(
                            RentalOutput(
                                rid = rs.getInt("rid"),
                                crid =
                                    CourtOutputCompleted(
                                        crid = rs.getInt("court_crid"),
                                        name = rs.getString("court_name"),
                                        club =
                                            ClubOutputWithIDCompleted(
                                                cid = rs.getInt("club_cid"),
                                                name = rs.getString("club_name"),
                                                owner =
                                                    UserOutputGetDetails(
                                                        uid = rs.getInt("club_owner_id"),
                                                        name = rs.getString("user_name"),
                                                        email = EmailSerial(rs.getString("user_email")),
                                                    ),
                                            ),
                                    ),
                                uid =
                                    UserDetails(
                                        uid = rs.getInt("user_uid"),
                                        name = rs.getString("user_name"),
                                        email = EmailSerial(rs.getString("user_email")),
                                        password = "PRIVATE",
                                        token = "PRIVATE",
                                    ),
                                datein = rs.getTimestamp("rental_date"),
                                datefim = rs.getTimestamp("rental_enddate"),
                                duration = rs.getInt("duration_hours"),
                            ),
                        )
                    }
                    return rentals
                }
            }
        }
    }

    override fun getAvailableHours(
        cid: Int,
        crid: Int,
        date: Timestamp,
    ): HoursList {
        val availableHours = mutableListOf<String>()

        dataSource.connection.use { conn ->
            // Valida se a quadra pertence ao clube
            val validateCourtSql = "SELECT 1 FROM Court WHERE crid = ? AND cid = ?"
            conn.prepareStatement(validateCourtSql).use { stm ->
                stm.setInt(1, crid)
                stm.setInt(2, cid)
                val rs = stm.executeQuery()
                if (!rs.next()) {
                    // throw IllegalArgumentException("Court ID $crid não pertence ao Club ID $cid")
                    throw InternalError.InvalidParameter("Court $crid does not belong to club $cid")
                }
            }

            // Valida se a data existe em algum aluguel
            val validateDateSql = "SELECT 1 FROM Rental WHERE DATE(rental_date) = ?"
            conn.prepareStatement(validateDateSql).use { stm ->
                stm.setTimestamp(1, date)
                val rs = stm.executeQuery()
            }

            // Consulta SQL para encontrar as horas disponíveis
            val sql =
                """
                WITH occupied_hours AS (
                    SELECT 
                        EXTRACT(HOUR FROM r.rental_date) AS start_hour, 
                        EXTRACT(HOUR FROM r.rental_enddate) AS end_hour
                    FROM Rental r
                    WHERE r.crid = ? 
                      AND DATE(r.rental_date) = ? 
                )
                SELECT TO_CHAR((t.hour * INTERVAL '1 hour')::time, 'HH24:MI:SS') AS available_hour
                FROM (
                    SELECT generate_series(0, 23) AS hour
                ) t
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM occupied_hours
                    WHERE t.hour >= occupied_hours.start_hour
                      AND t.hour < occupied_hours.end_hour
                )
                """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setInt(1, crid)
                stm.setTimestamp(2, date)

                stm.executeQuery().use { rs ->
                    while (rs.next()) {
                        val availableHour = rs.getString("available_hour")
                        availableHours.add(availableHour)
                    }
                }
            }
        }

        return HoursList(availableHours)
    }

    override fun deleteRental(rid: Int, token: String): Boolean {
        dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {

                val uid = conn.prepareStatement("SELECT uid FROM User_Table WHERE token = ?").use { stm ->
                    stm.setString(1, token)
                    stm.executeQuery().use { rs ->
                        if (rs.next()) {
                            rs.getInt("uid")
                        } else {
                            throw InternalError.UserNotFound
                        }
                    }
                }


                val isOwner = conn.prepareStatement("SELECT 1 FROM Rental WHERE rid = ? AND uid = ?").use { stm ->
                    stm.setInt(1, rid)
                    stm.setInt(2, uid)
                    stm.executeQuery().next()
                }

                if (!isOwner) {
                    throw InternalError.NotAuthorized
                }

                val rowsAffected = conn.prepareStatement("DELETE FROM Rental WHERE rid = ?").use { stm ->
                    stm.setInt(1, rid)
                    stm.executeUpdate()
                }

                conn.commit()
                return rowsAffected > 0
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }


    override fun updateRental(
        rid: Int,
        date: Timestamp,
        duration: Int,
    ): RentalOutput {
        dataSource.connection.use { conn ->
            val startDateTime = date.toLocalDateTime()
            val endOfDay = startDateTime.toLocalDate().atTime(23, 59, 59)

            val endDateTime = if (startDateTime.plusHours(duration.toLong()).isAfter(endOfDay)) {
                endOfDay
            } else {
                startDateTime.plusHours(duration.toLong())
            }

            val actualDuration =
                java.time.Duration
                    .between(startDateTime, endDateTime)
                    .toHours()
                    .toInt()

            val sql =
                """
            UPDATE Rental 
            SET rental_date = ?, rental_enddate = ?, duration_hours = ? 
            WHERE rid = ?
            """.trimIndent()

            conn.prepareStatement(sql).use { stm ->
                stm.setTimestamp(1, Timestamp.valueOf(startDateTime))
                stm.setTimestamp(2, Timestamp.valueOf(endDateTime))
                stm.setInt(3, actualDuration)
                stm.setInt(4, rid)

                val rowsAffected = stm.executeUpdate()
                if (rowsAffected == 0) {
                    throw RuntimeException("Rental not found or not authorized to update.")
                }
            }

            return getRentalById(rid)
        }
    }
}


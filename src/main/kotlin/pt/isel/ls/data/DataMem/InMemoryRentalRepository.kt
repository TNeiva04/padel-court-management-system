package pt.isel.ls.data.DataMem

import pt.isel.ls.data.RentalRepository
import pt.isel.ls.domain.Rental
import pt.isel.ls.error.InternalError
import pt.isel.ls.http.Web.API.models.*
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.LocalTime

val Rentals =
    mutableListOf(
        Rental(
            rid = 1,
            crid = Courts[0],
            uid = Users[0],
            datein = Timestamp.valueOf("2021-10-10 14:00:00"),
            datefim = Timestamp.valueOf("2021-10-10 15:00:00"),
            duration = 1,
        ),
        Rental(
            rid = 2,
            crid = Courts[1],
            uid = Users[1],
            datein = Timestamp.valueOf("2021-10-10 16:00:00"),
            datefim = Timestamp.valueOf("2021-10-10 18:00:00"),
            duration = 2,
        ),
        Rental(
            rid = 3,
            crid = Courts[2],
            uid = Users[2],
            datein = Timestamp.valueOf("2021-10-10 18:00:00"),
            datefim = Timestamp.valueOf("2021-10-10 21:00:00"),
            duration = 3,
        ),
    )

class InMemoryRentalRepository : RentalRepository {
    override fun createRental(
        cid: Int,
        crid: Int,
        datein: Timestamp,
        duration: Int,
        uid: Int,
    ): OutputRental {
        val user = Users.find { it.uid == uid } ?: throw IllegalArgumentException("User not found")
        val club = Clubs.find { it.cid == cid } ?: throw IllegalArgumentException("Club not found")
        val court =
            Courts.find { it.crid == crid && it.club == club }
                ?: throw InternalError.ClubNotFound("Club with ID $cid not found.")

        val rid = Rentals.size + 1

        // Calcula o datefim com base na duração (em horas)
        val millisToAdd = duration * 60 * 60 * 1000L // horas → ms
        val datefim = Timestamp(datein.time + millisToAdd)

        val rental =
            Rental(
                rid = rid,
                crid = court,
                uid = user,
                datein = datein,
                datefim = datefim,
                duration = duration,
            )

        Rentals.add(rental)
        return OutputRental(rental.rid)
    }

    override fun getRentalById(rid: Int): RentalOutput =
        Rentals.find { it.rid == rid }?.let { rental ->
            RentalOutput(
                rid = rental.rid,
                crid =
                    CourtOutputCompleted(
                        crid = rental.crid.crid,
                        name = rental.crid.name,
                        club =
                            ClubOutputWithIDCompleted(
                                cid = rental.crid.club.cid,
                                name = rental.crid.club.name,
                                owner =
                                    UserOutputGetDetails(
                                        uid = rental.crid.club.owner.uid,
                                        name = rental.crid.club.owner.name,
                                        email = EmailSerial(rental.crid.club.owner.email.email),
                                    ),
                            ),
                    ),
                uid =
                    UserDetails(
                        uid = rental.uid.uid,
                        name = rental.uid.name,
                        email = EmailSerial(rental.uid.email.email),
                        password = "PRIVATE",
                        token = "PRIVATE",
                    ),
                datein = rental.datein,
                datefim = rental.datefim,
                duration = rental.duration,
            )
        } ?: throw InternalError.RentalNotFound("$rid")

    override fun getRentalsByClubCourtDate(
        cid: Int,
        crid: Int,
        date: Timestamp,
    ): List<RentalOutput> =
        Rentals
            .filter {
                it.crid.club.cid == cid &&
                        it.crid.crid == crid &&
                        it.datein.toLocalDateTime().toLocalDate() == date.toLocalDateTime().toLocalDate()
            }.map { rental ->
                val millisToAdd = (rental.duration * 60 * 60 * 1000).toLong()
                val datefim = Timestamp(rental.datein.time + millisToAdd)

                RentalOutput(
                    rid = rental.rid,
                    crid =
                        CourtOutputCompleted(
                            crid = rental.crid.crid,
                            name = rental.crid.name,
                            club =
                                ClubOutputWithIDCompleted(
                                    cid = rental.crid.club.cid,
                                    name = rental.crid.club.name,
                                    owner =
                                        UserOutputGetDetails(
                                            uid = rental.crid.club.owner.uid,
                                            name = rental.crid.club.owner.name,
                                            email = EmailSerial(rental.crid.club.owner.email.email),
                                        ),
                                ),
                        ),
                    uid =
                        UserDetails(
                            uid = rental.uid.uid,
                            name = rental.uid.name,
                            email = EmailSerial(rental.uid.email.email),
                            password = "PRIVATE",
                            token = "PRIVATE",
                        ),
                    datein = rental.datein,
                    datefim = datefim,
                    duration = rental.duration,
                )
            }

    override fun getRentalsByUser(uid: Int): List<RentalOutput> =
        Rentals.filter { it.uid.uid == uid }.map { rental ->
            // Calcula o datefim com base na duração (em horas)
            val datefim = Timestamp(rental.datein.time + rental.duration * 60 * 60 * 1000L)

            RentalOutput(
                rid = rental.rid,
                crid =
                    CourtOutputCompleted(
                        crid = rental.crid.crid,
                        name = rental.crid.name,
                        club =
                            ClubOutputWithIDCompleted(
                                cid = rental.crid.club.cid,
                                name = rental.crid.club.name,
                                owner =
                                    UserOutputGetDetails(
                                        uid = rental.crid.club.owner.uid,
                                        name = rental.crid.club.owner.name,
                                        email = EmailSerial(rental.crid.club.owner.email.email),
                                    ),
                            ),
                    ),
                uid =
                    UserDetails(
                        uid = rental.uid.uid,
                        name = rental.uid.name,
                        email = EmailSerial(rental.uid.email.email),
                        password = "PRIVATE",
                        token = "PRIVATE",
                    ),
                datein = rental.datein,
                datefim = datefim,
                duration = rental.duration,
            )
        }

    override fun getAvailableHours(
        cid: Int,
        crid: Int,
        date: Timestamp,
    ): HoursList {
        val rentals =
            Rentals.filter {
                it.crid.club.cid == cid &&
                        it.crid.crid == crid &&
                        it.datein.toLocalDateTime().toLocalDate() == date.toLocalDateTime().toLocalDate()
            }

        val occupiedHours = mutableSetOf<Int>()

        rentals.forEach { rental ->
            val start = rental.datein.toLocalDateTime().hour
            val end = rental.datefim.toLocalDateTime().hour

            for (hour in start until end) {
                occupiedHours.add(hour)
            }
        }

        val availableHours = mutableListOf<String>()
        val baseDate = date.toLocalDateTime().toLocalDate()

        for (hour in 0..23) {
            if (hour !in occupiedHours) {
                val dateTime = LocalDateTime.of(baseDate, LocalTime.of(hour, 0))
                availableHours.add(dateTime.toString().replace('T', ' '))
            }
        }

        return HoursList(availableHours)
    }

    override fun getRentals(): List<RentalOutput> =
        Rentals.map { rental ->
            val millisToAdd = (rental.duration * 60 * 60 * 1000).toLong()
            val datefim = Timestamp(rental.datein.time + millisToAdd)

            RentalOutput(
                rid = rental.rid,
                crid =
                    CourtOutputCompleted(
                        crid = rental.crid.crid,
                        name = rental.crid.name,
                        club =
                            ClubOutputWithIDCompleted(
                                cid = rental.crid.club.cid,
                                name = rental.crid.club.name,
                                owner =
                                    UserOutputGetDetails(
                                        uid = rental.crid.club.owner.uid,
                                        name = rental.crid.club.owner.name,
                                        email = EmailSerial(rental.crid.club.owner.email.email),
                                    ),
                            ),
                    ),
                uid =
                    UserDetails(
                        uid = rental.uid.uid,
                        name = rental.uid.name,
                        email = EmailSerial(rental.uid.email.email),
                        password = "PRIVATE",
                        token = "PRIVATE",
                    ),
                datein = rental.datein,
                datefim = datefim,
                duration = rental.duration,
            )
        }

    override fun deleteRental(rid: Int, token: String): Boolean {
        val user = Users.find { it.token == token }
            ?: throw InternalError.UserNotFound

        val rental = Rentals.find { it.rid == rid && it.uid.uid == user.uid }
            ?: return false // rental não encontrado ou não pertence ao user

        Rentals.remove(rental)
        return true
    }


    override fun updateRental(
        rid: Int,
        date: Timestamp,
        duration: Int,
    ): RentalOutput {
        val rental =
            Rentals.find { it.rid == rid } ?: throw InternalError.RentalNotFound("Rental with ID $rid not found.")

        // Atualiza os valores da instância original
        rental.datein = date
        rental.duration = duration

        // Calcula o novo datefim com base na duração
        val datefim = Timestamp(date.time + duration * 60 * 60 * 1000L)

        return RentalOutput(
            rid = rental.rid,
            crid =
                CourtOutputCompleted(
                    crid = rental.crid.crid,
                    name = rental.crid.name,
                    club =
                        ClubOutputWithIDCompleted(
                            cid = rental.crid.club.cid,
                            name = rental.crid.club.name,
                            owner =
                                UserOutputGetDetails(
                                    uid = rental.crid.club.owner.uid,
                                    name = rental.crid.club.owner.name,
                                    email = EmailSerial(rental.crid.club.owner.email.email),
                                ),
                        ),
                ),
            uid =
                UserDetails(
                    uid = rental.uid.uid,
                    name = rental.uid.name,
                    email = EmailSerial(rental.uid.email.email),
                    password = "PRIVATE",
                    token = "PRIVATE",
                ),
            datein = rental.datein,
            datefim = datefim,
            duration = rental.duration,
        )
    }
}

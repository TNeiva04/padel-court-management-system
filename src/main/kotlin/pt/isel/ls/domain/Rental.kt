package pt.isel.ls.domain

import java.sql.Timestamp

data class Rental(val rid: Int, val crid: Court, val uid: User, var datein: Timestamp, var datefim: Timestamp, var duration: Int)

data class Duration(val duration: Int)


package pt.isel.ls.http.Web.API.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.sql.Timestamp


@Serializable
data class DurationInput (val duration:Int)
@Serializable
data class RentalInput(val date: String, val Duration: DurationInput)

@Serializable
data class OutputRental(val rid: Int)

@Serializer(forClass = Timestamp::class)
object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeString(value.toString()) // ISO format
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.valueOf(decoder.decodeString())
    }
}

@Serializable
data class DurationOutput (val duration:Int)
@Serializable
data class RentalOutput(
    val rid: Int,
    val crid: CourtOutputCompleted,
    val uid: UserDetails,
    @Serializable(with = TimestampSerializer::class) var datein: Timestamp,
    @Serializable(with = TimestampSerializer::class) val datefim: Timestamp,
    var duration: Int
)

@Serializable
data class HoursList(val hours: MutableList<String>) {
    fun drop(skip: Int): HoursList {
        return HoursList(hours.drop(skip).toMutableList())
    }

    fun take(take: Int): HoursList {
        return HoursList(hours.take(take).toMutableList())
    }
}

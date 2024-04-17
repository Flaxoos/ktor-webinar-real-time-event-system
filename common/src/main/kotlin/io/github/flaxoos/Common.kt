package io.github.flaxoos

import com.sksamuel.avro4k.AvroNamespace
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.format.DateTimeFormatter

fun localNow() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

object LocalTimeSerializer : KSerializer<LocalTime> {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalTime {
        return java.time.LocalTime.parse(decoder.decodeString()).toKotlinLocalTime()
    }

    override fun serialize(encoder: Encoder, value: LocalTime) {
        val string = timeFormatter.format(value.toJavaLocalTime())
        encoder.encodeString(string)
    }
}

@Serializable
@AvroNamespace("io.github.flaxoos")
data class MyEvent(
    @Serializable(LocalTimeSerializer::class)
    val time: LocalTime = localNow(),
    val message: String = "My event",
)

const val CONSUMER_PORT = 8084
const val PRODUCER_PORT = 8082
const val BOOTSTRAP_SERVERS = "localhost:29092"
const val SCHEMA_REGISTRY_URL = "http://localhost:8081"
const val TOPIC_NAME = "events"


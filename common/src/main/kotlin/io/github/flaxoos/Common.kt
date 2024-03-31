package io.github.flaxoos

import com.sksamuel.avro4k.AvroNamespace
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

fun localNow() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

@Serializable
@AvroNamespace("io.github.flaxoos")
data class MyEvent(
    val timestamp: LocalDateTime = localNow(),
    val message: String = "My event",
)

const val PROCESSOR_PORT = 8083
const val CONSUMER_PORT = 8082
const val BOOTSTRAP_SERVERS = "localhost:29092"
const val SCHEMA_REGISTRY_URL = "http://localhost:8081"

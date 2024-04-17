package io.github.flaxoos

import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.components.fromRecord
import io.github.flaxoos.ktor.server.plugins.kafka.consumer
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
import io.github.flaxoos.ktor.server.plugins.kafka.installKafka
import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.collections.ConcurrentSet
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(
        CIO,
        port = CONSUMER_PORT,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()

    configureKafka()
}

fun Application.configureRouting() {
    routing {
        configureRateLimiting()
        get("event") {
            eventStore.lastOrNull()?.let { call.respond(it) } ?: call.respond(NotFound)
        }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.onEvent(event: MyEvent) {
    eventStore.add(event)
    log.info("Received event: ${event.message}")
}

val eventStore = ConcurrentSet<MyEvent>()

fun Application.configureKafka() {
    install(RateLimiting)
    installKafka {
        val events = TopicName.named(TOPIC_NAME)
        schemaRegistryUrl = SCHEMA_REGISTRY_URL
        consumer {
            bootstrapServers = BOOTSTRAP_SERVERS
            groupId = "consumer-group"
            clientId = "consumer"
        }

        consumerConfig {
            consumerRecordHandler(events) { record ->
                val event = fromRecord<MyEvent>(record.value())
                onEvent(event)
            }
        }
    }
}

fun Route.configureRateLimiting() {
    install(RateLimiting) {
        rateLimiter {
            capacity = 20
            rate = 4.seconds
            type = TokenBucket::class
        }
    }
}

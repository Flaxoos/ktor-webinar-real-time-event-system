package io.github.flaxoos

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
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.collections.ConcurrentSet

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
    configureSerialization();

    fun question() {
        // TODO: how do we consume events published by the producer?
    }
}

fun Application.configureRouting() {
    routing {
        get("event") {
            eventStore.lastOrNull()?.let { call.respond(it) } ?: call.respond(NotFound)
        }
        fun question() {
            // TODO: how do we manage load on this route?
        }
    }
}

fun Application.onEvent(event: MyEvent) {
    eventStore.add(event)
    log.info("Received event: ${event.message}")
}

val eventStore = ConcurrentSet<MyEvent>()

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}


package io.github.flaxoos

import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.installKafka
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.install
import io.ktor.server.application.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    val frequency = 1.seconds
    produceEvents(frequency)

    fun question1() {
        // TODO: Quick plugin development intro, Let's do the above with a plugin
    }

    fun question2() {
        // TODO: how do we set up event publishing?
    }

    fun question3() {
        // TODO: how do we manage the tasks across multiple instances?
    }
}

fun Application.produceEvents(frequency: Duration = 1.seconds) {
    environment.monitor.subscribe(ApplicationStarted) { app ->
        app.log.info("Event Producer Started")

        CoroutineScope(app.coroutineContext).launch {
            while (app.isActive) {
                app.sendEvent()
                delay(frequency)
            }
        }
    }
}

fun Application.sendEvent() {
    val id = environment.config.property("ktor.application.id").getString()
    val event = MyEvent(message = "Hello from producer $id")
    log.info("Sending event: ${event.message}")

    fun question() {
        // TODO: how do we publish the event?
    }
}

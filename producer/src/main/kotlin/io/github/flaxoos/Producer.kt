package io.github.flaxoos

import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.components.toRecord
import io.github.flaxoos.ktor.server.plugins.kafka.installKafka
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskScheduling
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.redis.redis
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
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    val frequency = 1.seconds
    produceEvents(frequency)
    //TODO: can we do this with a plugin?

    // TODO: how do we manage the tasks across multiple instances?
    // TODO: how do we setup kafka to produce events?
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

val produceEventsPlugin =
    createApplicationPlugin("produceEvents", ::ProduceEventsConfig) {
        on(MonitoringEvent(ApplicationStarted)) { app ->
            app.log.info("Event Producer Started")

            CoroutineScope(app.coroutineContext).launch {
                while (app.isActive) {
                    app.sendEvent()
                    delay(pluginConfig.frequency)
                }
            }
        }
    }

class ProduceEventsConfig {
    var frequency: Duration = 1.seconds
}

fun Application.sendEvent() {
    val id = environment.config.property("ktor.application.id").getString()
    val event = MyEvent(message = "Hello from producer $id")
    log.info("Sending event: ${event.message}")

    // TODO: how do we send the event to kafka?
}

// --------------------------------------------------------------------

fun Application.configureKafka() {
    installKafka {
        val events = TopicName.named(TOPIC_NAME)
        schemaRegistryUrl = SCHEMA_REGISTRY_URL
        topic(events) {
            partitions = 1
            replicas = 1
        }
        producer {
            bootstrapServers = BOOTSTRAP_SERVERS
            retries = 1
            clientId = "producer"
        }
        registerSchemas {
            MyEvent::class at events
        }
    }
}

fun Application.configureTasks() {
    install(TaskScheduling) {
        val redisManagerName = "my-redis-manager"
        redis(redisManagerName) {
            host = "localhost"
            port = 6379
            username = "flaxoos"
            password = "password"
            connectionAcquisitionTimeoutMs = 1000
            lockExpirationMs = 4500
        }
        task(redisManagerName) {
            name = "produce-events"
            task = {
                sendEvent()
            }
            kronSchedule = {
                seconds {
                    1 every 3
                }
            }
        }
    }
}

fun Application.sendKafkaEvent(event: MyEvent) {
    this.kafkaProducer?.send(ProducerRecord("events", event.time.toString(), event.toRecord()))
        ?.get(100, TimeUnit.MILLISECONDS)
}

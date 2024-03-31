package io.github.flaxoos

import io.github.flaxoos.ktor.client.plugins.circuitbreaker.CircuitBreakerName.Companion.toCircuitBreakerName
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.CircuitBreaking
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.global
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.register
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.requestWithCircuitBreaker
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.HttpMethod.Companion.Get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

const val CLIENTS_COUNT = 3
val strict = "strict".toCircuitBreakerName()

fun main() {
    runBlocking {
        List(CLIENTS_COUNT) {
            val logger = LoggerFactory.getLogger("Client: $it")
            val scope = CoroutineScope(Dispatchers.IO)
            val client =
                HttpClient(CIO) {
                    install(CircuitBreaking) {
                        global {
                            failureThreshold = 1
                            halfOpenFailureThreshold = 5
                            resetInterval = 1.seconds
                        }

                        register(strict) {
                            failureThreshold = 1
                            halfOpenFailureThreshold = 2
                            resetInterval = 2.seconds
                        }
                    }
                }
            scope.launch {
                while (isActive) {
                    logger.info("Getting events")
                    runCatching {
                        val response =
                            client.requestWithCircuitBreaker {
                                method = Get
                                url("http://localhost:$PROCESSOR_PORT/event")
                            }
                        logger.error("Events Response: $response")
                    }.onFailure {
                        logger.info("Error: ${it.message}")
                    }
                    delay(1000)
                }
            }
        }.joinAll()
    }
}

package io.github.flaxoos

import io.github.flaxoos.ktor.client.plugins.circuitbreaker.CircuitBreakerName.Companion.toCircuitBreakerName
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.CircuitBreaking
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.global
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.register
import io.github.flaxoos.ktor.client.plugins.circuitbreaker.requestWithCircuitBreaker
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
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

const val CLIENTS_COUNT = 2
val strict = "strict".toCircuitBreakerName()

fun main() {
    runBlocking {
        List(CLIENTS_COUNT) {
            val logger = LoggerFactory.getLogger("Client: $it")
            val scope = CoroutineScope(Dispatchers.IO)
            val client =
                HttpClient(CIO) {
                    setupCircuitBreaking()
                }
            scope.launch {
                while (isActive) {
                    logger.info("Getting events")
                    runCatching {
                        val response =
                            // TODO: how do we prevent repeating requests when the server is failing?
                            client.requestWithCircuitBreaker(strict) {
                                method = Get
                                url("http://localhost:$CONSUMER_PORT/event")
                            }
                        logger.info("Events Response: $response")
                    }.onFailure {
                        logger.error("Error: ${it::class.simpleName}: ${it.message}")
                    }
                    delay(1000)
                }
            }
        }.joinAll()
    }
}

// --------------------------------------

private fun HttpClientConfig<CIOEngineConfig>.setupCircuitBreaking() {
    install(CircuitBreaking) {
        global {
            failureThreshold = 1
            halfOpenFailureThreshold = 5
            resetInterval = 1.seconds
        }

        register(strict) {
            failureThreshold = 1
            halfOpenFailureThreshold = 1
            resetInterval = 5.seconds
        }
    }
}

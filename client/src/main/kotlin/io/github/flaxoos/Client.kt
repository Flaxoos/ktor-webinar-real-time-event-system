package io.github.flaxoos

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
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

const val CLIENTS_COUNT = 2

fun main() {
    runBlocking {
        List(CLIENTS_COUNT) {
            val logger = LoggerFactory.getLogger("Client: $it")
            val scope = CoroutineScope(Dispatchers.IO)
            val client =
                HttpClient(CIO)
            scope.launch {
                while (isActive) {
                    logger.info("Getting events")
                    runCatching {
                        val response =
                            client.request{
                                method = Get
                                url("http://localhost:$CONSUMER_PORT/event");

                                fun question() {
                                    // TODO: how do we prevent repeating requests when the server is failing?
                                }
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

package net.redstonecraft

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.util.*
import io.ktor.features.*
import io.ktor.content.*
import io.ktor.http.content.*
import org.slf4j.event.*
import io.ktor.serialization.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*
import net.redstonecraft.plugins.*

class ApplicationTest {

    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World!", response.content)
            }
        }
    }
}
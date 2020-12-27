package io.krugosvet.dailydish

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(ContentNegotiation) {
        gson {}
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
    }

    routing {
        get("/") {
            val message = Gson().toJson(mapOf("hello" to "world"))

            call.respond(message)
        }
    }
}


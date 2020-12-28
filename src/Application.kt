package io.krugosvet.dailydish

import com.google.gson.Gson
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.db.entity.Meal
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

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

    DatabaseHelper.connect()

    routing {
        get("/") {
            val message = transaction { Meal.all().map { it.toString() } }
            val json = Gson().toJson(mapOf("data" to message))

            call.respond(json)
        }
    }
}

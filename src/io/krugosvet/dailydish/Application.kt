package io.krugosvet.dailydish

import io.krugosvet.dailydish.injection.configModule
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.injection.repositoryModule
import io.krugosvet.dailydish.route.mealRouting
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
  install(DefaultHeaders)
  install(CallLogging)

  install(Koin) {
    modules(repositoryModule, configModule)
  }

  install(ContentNegotiation) {
    json()
  }

  install(Compression) {
    gzip {
      priority = 1.0
    }
  }

  get<DatabaseHelper>().connect()

  routing {
    static("/static") {
      resources("static")
    }
    mealRouting()
  }
}
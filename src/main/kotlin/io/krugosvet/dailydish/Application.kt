package io.krugosvet.dailydish

import io.krugosvet.dailydish.common.core.commonModules
import io.krugosvet.dailydish.route.mealRouting
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.koin.ktor.ext.Koin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
  install(DefaultHeaders)
  install(CallLogging)

  install(Koin) {
    modules(commonModules)
  }

  install(ContentNegotiation) {
    json()
  }

  install(Compression) {
    gzip {
      priority = 1.0
    }
  }

  routing {
    static("/static") {
      files("src/main/resources/static")
    }
    mealRouting()
  }
}

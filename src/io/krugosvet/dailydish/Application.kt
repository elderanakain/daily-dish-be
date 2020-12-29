package io.krugosvet.dailydish

import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.injection.repositoryModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.*
import io.ktor.routing.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import java.text.DateFormat

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.main() {
  install(DefaultHeaders)
  install(CallLogging)

  install(Koin) {
    modules(repositoryModule)
  }

  install(ContentNegotiation) {
    gson {
      setDateFormat(DateFormat.LONG)
    }
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
  }
}
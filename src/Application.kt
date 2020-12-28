package io.krugosvet.dailydish

import com.google.gson.Gson
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.injection.repositoryModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
  install(DefaultHeaders)
  install(CallLogging)

  install(Koin) {
    modules(repositoryModule)
  }

  install(ContentNegotiation) {
    gson {}
  }

  install(Compression) {
    gzip {
      priority = 1.0
    }
  }

  get<DatabaseHelper>().connect()

  val mealRepository: MealRepository by inject()
  val gson: Gson by inject()

  routing {
    get("/") {
      val meals = mealRepository.meals
      val json = gson.toJson(meals)

      call.respond(json)
    }
  }
}

package io.krugosvet.dailydish

import com.google.gson.Gson
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.dto.Meal
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.get

@Suppress("unused") // Referenced in application.conf
fun Application.module(
  mealRepository: MealRepository = get(),
  gson: Gson = get(),
  dispatchers: Dispatchers = get(),
) {

  routing {
    get("meal") {
      val meals = mealRepository.meals
      val json = gson.toJson(meals)

      call.respondText(json, ContentType.Application.Json, HttpStatusCode.OK)
    }

    post("meal") {
      withContext(dispatchers.IO) {
        val meal = call.receive<Meal>()

        mealRepository.add(meal)

        call.respond(HttpStatusCode.Created)
      }
    }
  }
}
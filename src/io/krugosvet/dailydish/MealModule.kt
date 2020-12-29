package io.krugosvet.dailydish

import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.dto.Meal
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.get as koinGet

@Suppress("unused") // Referenced in application.conf
fun Application.module(
  mealRepository: MealRepository = koinGet(),
  dispatchers: Dispatchers = koinGet(),
) {

  routing {
    route("meal") {
      get {
        val meals = mealRepository.meals

        call.respond(HttpStatusCode.OK, meals)
      }

      post {
        withContext(dispatchers.IO) {
          val meal = call.receive<Meal>()

          mealRepository.add(meal)

          call.respond(HttpStatusCode.Created)
        }
      }
    }
  }
}
package io.krugosvet.dailydish.route

import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.dto.Meal
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject

fun Route.mealRouting() {
  val mealRepository: MealRepository by inject()
  val dispatchers: Dispatchers by inject()

  route("meal") {
    get {
      runCatching {mealRepository.meals }
        .onSuccess { meals -> call.respond(OK, meals) }
        .onFailure { call.respond(BadRequest) }
    }

    get("/{id}") {
      val id = call.getIdFromParams() ?: return@get

      runCatching { mealRepository.get(id) }
        .onSuccess { meal -> call.respond(OK, meal) }
        .onFailure { call.respond(BadRequest) }
    }

    post {
      withContext(dispatchers.IO) {
        val meal = call.receive<Meal>()

        runCatching { mealRepository.add(meal) }
          .onSuccess { id -> call.respond(Created, id) }
          .onFailure { call.respond(BadRequest) }
      }
    }

    delete("{id}") {
      val id = call.getIdFromParams() ?: return@delete

      runCatching { mealRepository.delete(id) }
        .onSuccess { call.respond(Accepted) }
        .onFailure { call.respond(BadRequest) }
    }
  }
}

private suspend fun ApplicationCall.getIdFromParams(): String? {
  val id = parameters["id"]?.takeIf { it.isNotBlank() }

  if (id == null) {
    respond(BadRequest)
    return null
  }

  return id
}

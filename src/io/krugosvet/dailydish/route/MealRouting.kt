package io.krugosvet.dailydish.route

import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.dto.AddMeal
import io.krugosvet.dailydish.repository.dto.Meal
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.io.*
import java.net.URI

fun Route.mealRouting() {
  val mealRepository: MealRepository by inject()
  val dispatchers: Dispatchers by inject()

  route("meal") {
    get {
      runCatching { mealRepository.meals }
        .onSuccess { meals -> call.respond(OK, meals) }
        .onFailure { call.respond(BadRequest) }
    }

    get("{id}") {
      val id = call.getIdFromParams() ?: return@get

      runCatching { mealRepository.get(id) }
        .onSuccess { meal -> call.respond(OK, meal) }
        .onFailure { call.respond(BadRequest) }
    }

    post {
      withContext(dispatchers.IO) {
        runCatching {
          var meal: AddMeal? = null
          var imageUri: URI? = null

          call.receiveMultipart()
            .forEachPart { part ->
              when (part) {
                is PartData.FormItem -> {
                  call.application.log.info("Form parsing ${part.value}")

                  meal = Json.decodeFromString(part.value)
                }
                is PartData.FileItem -> {
                  call.application.log.info("FileItem parsing")

                  imageUri = mealRepository.saveImage(part.streamProvider(), part.contentType?.contentSubtype ?: "jpg")
                }
              }

              part.dispose()
            }

          mealRepository.add(meal!!.copy(image = imageUri.toString()))
        }
          .onSuccess { id -> call.respond(Created, id) }
          .onFailure {
            call.application.log.error(it)
            call.respond(BadRequest)
          }
      }
    }

    delete("{id}") {
      val id = call.getIdFromParams() ?: return@delete

      runCatching { mealRepository.delete(id) }
        .onSuccess { call.respond(Accepted) }
        .onFailure { call.respond(BadRequest) }
    }

    put {
      withContext(dispatchers.IO) {
        val meal = call.receive<Meal>()

        runCatching { mealRepository.update(meal) }
          .onSuccess { call.respond(Accepted) }
          .onFailure { call.respond(BadRequest) }
      }
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

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

fun Route.mealRouting() {
  val mealRepository: MealRepository by inject()

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

    createMealRoute()

    delete("{id}") {
      val id = call.getIdFromParams() ?: return@delete

      runCatching { mealRepository.delete(id) }
        .onSuccess { call.respond(Accepted) }
        .onFailure { call.respond(BadRequest) }
    }

    put {
      withContext(Dispatchers.IO) {
        val meal = call.receive<Meal>()

        runCatching { mealRepository.update(meal) }
          .onSuccess { call.respond(Accepted) }
          .onFailure { call.respond(BadRequest) }
      }
    }
  }
}

private fun Route.createMealRoute() {
  val mealRepository: MealRepository by inject()

  post {
    withContext(Dispatchers.IO) {
      runCatching {
        var meal: AddMeal? = null
        var imageUri: String? = null

        call.receiveMultipart()
          .forEachPart { part ->
            when (part) {
              is PartData.FormItem -> {
                meal = Json.decodeFromString(part.value)
              }
              is PartData.FileItem -> {
                val imageExtension = part.contentType?.contentSubtype ?: ContentType.Image.JPEG.toString()

                imageUri = mealRepository.saveImage(part.streamProvider(), imageExtension)
              }
              is PartData.BinaryItem -> return@forEachPart
            }

            part.dispose()
          }

        mealRepository.add(
          meal!!.copy(image = imageUri)
        )
      }
        .onSuccess { id ->
          call.respond(Created, id)
        }
        .onFailure {
          call.application.log.error(it)
          call.respond(BadRequest)
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

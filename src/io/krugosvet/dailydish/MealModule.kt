package io.krugosvet.dailydish

import com.google.gson.Gson
import io.krugosvet.dailydish.repository.MealRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Application.module() {

  val mealRepository: MealRepository by inject()
  val gson: Gson by inject()

  routing {
    get("/") {
      val meals = mealRepository.meals
      val json = gson.toJson(meals)

      call.respondText(json, ContentType.Application.Json, HttpStatusCode.OK)
    }
  }
}
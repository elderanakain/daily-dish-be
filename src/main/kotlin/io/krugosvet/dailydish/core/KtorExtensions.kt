package io.krugosvet.dailydish.core

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*

suspend inline fun <T> Result<T>.onCallFailure(call: ApplicationCall): Result<T> =
  onFailure {
    call.application.log.error(it)
    call.respond(HttpStatusCode.BadRequest)
  }

suspend fun ApplicationCall.getIdFromParams(): String? {
  val id = parameters["id"]?.takeIf { it.isNotBlank() }

  if (id == null) {
    respond(HttpStatusCode.BadRequest)
    return null
  }

  return id
}

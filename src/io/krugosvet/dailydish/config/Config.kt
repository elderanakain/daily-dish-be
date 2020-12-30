@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.krugosvet.dailydish.config

import io.ktor.config.*

class Config(
  private val appConfig: HoconApplicationConfig
) {

  val dbUrl: String by lazy {
    when (appConfig.property("ktor.deployment.environment").getString()) {
      "prod" -> "jdbc:sqlite:/tmp/data/data.db"
      "test" -> "jdbc:sqlite:/tmp/data/test_data.db"
      else -> "jdbc:sqlite:/tmp/data/data.db"
    }
  }

}
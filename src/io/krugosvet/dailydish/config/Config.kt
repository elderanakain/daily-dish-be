@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.krugosvet.dailydish.config

import io.ktor.config.*

class Config(
  private val appConfig: HoconApplicationConfig
) {

  val dbUrl: String by lazy {
    "jdbc:sqlite:${appConfig.property("ktor.deployment.db").getString()}"
  }

}

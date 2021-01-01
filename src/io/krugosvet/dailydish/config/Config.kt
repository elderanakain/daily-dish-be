@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.krugosvet.dailydish.config

import io.ktor.config.*

class Config(
  private val appConfig: HoconApplicationConfig
) {

  val dbUrl: String by lazy {
    appConfig.property("ktor.deployment.db").getString()
  }

  val dbUser: String by lazy {
    appConfig.property("ktor.deployment.db_username").getString()
  }

  val dbPassword: String by lazy {
    appConfig.property("ktor.deployment.db_password").getString()
  }

}

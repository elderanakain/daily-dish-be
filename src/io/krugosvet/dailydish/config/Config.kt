@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.krugosvet.dailydish.config

import io.ktor.config.*

class Config(
  private val appConfig: HoconApplicationConfig
) {

  val host: String by lazy {
    appConfig.property("ktor.deployment.host").getString()
  }

  val port: String by lazy {
    appConfig.property("ktor.deployment.port").getString()
  }

}
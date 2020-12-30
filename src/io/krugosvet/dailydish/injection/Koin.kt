package io.krugosvet.dailydish.injection

import com.typesafe.config.ConfigFactory
import io.krugosvet.dailydish.config.Config
import io.ktor.config.*
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val configModule = module {

  single {
    Config(HoconApplicationConfig(ConfigFactory.load()))
  }

  single {
    Dispatchers
  }
}
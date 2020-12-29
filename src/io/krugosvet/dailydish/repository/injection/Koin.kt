package io.krugosvet.dailydish.repository.injection

import com.google.gson.GsonBuilder
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.MealRepositoryImpl
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.dto.MealFactory
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val repositoryModule = module {

  single {
    GsonBuilder()
      .create()
  }

  single {
    DatabaseHelper()
  }

  single {
    MealFactory()
  }

  single {
    MealDAO()
  }

  single<MealRepository> {
    MealRepositoryImpl(get(), get())
  }

  single {
    Dispatchers
  }
}
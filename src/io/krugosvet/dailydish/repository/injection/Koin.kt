package io.krugosvet.dailydish.repository.injection

import io.krugosvet.dailydish.repository.ImageRepository
import io.krugosvet.dailydish.repository.MealRepository
import io.krugosvet.dailydish.repository.MealRepositoryImpl
import io.krugosvet.dailydish.repository.db.DatabaseHelper
import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.dto.MealFactory
import org.koin.dsl.module

val repositoryModule = module {

  single {
    DatabaseHelper(get())
  }

  single {
    MealFactory()
  }

  single {
    MealDAO()
  }

  single {
    ImageRepository(get())
  }

  single<MealRepository> {
    MealRepositoryImpl(get(), get(), get())
  }
}

package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.dto.Meal
import io.krugosvet.dailydish.repository.dto.MealFactory
import org.jetbrains.exposed.sql.transactions.transaction

interface MealRepository {

  val meals: List<Meal>
}

class MealRepositoryImpl(
  private val mealDAO: MealDAO,
  private val mealFactory: MealFactory,
) :
  MealRepository {

  override val meals: List<Meal>
    get() = transaction {
      mealDAO.all()
        .toList()
        .map { entity ->
          mealFactory.from(entity)
        }
    }
}
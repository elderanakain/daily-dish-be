package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.dto.Meal
import io.krugosvet.dailydish.repository.dto.MealFactory
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface MealRepository {
  val meals: List<Meal>

  fun add(meal: Meal)
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

  override fun add(meal: Meal): Unit = transaction {
    mealDAO.new {
      title = meal.title
      description = meal.description
      imageUri = meal.image
      lastCookingDate = DateTime.parse(meal.lastCookingDate)
    }
  }
}
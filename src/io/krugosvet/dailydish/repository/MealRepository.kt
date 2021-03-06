package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.db.entity.MealEntity
import io.krugosvet.dailydish.repository.dto.AddMeal
import io.krugosvet.dailydish.repository.dto.Meal
import io.krugosvet.dailydish.repository.dto.MealFactory
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

interface MealRepository {
  val meals: List<Meal>

  suspend fun add(meal: AddMeal): String

  suspend fun delete(id: String)

  suspend fun get(id: String): Meal

  suspend fun update(meal: Meal)
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
        .map(::mapFromEntity)
    }

  override suspend fun add(meal: AddMeal): String = transaction {
    val newId = UUID.randomUUID().toString()

    mealDAO
      .new(newId) {
        title = meal.title
        description = meal.description
        imageUri = meal.image
        lastCookingDate = DateTime.parse(meal.lastCookingDate)
      }
      .id.value
  }

  override suspend fun delete(id: String): Unit = transaction {
    mealDAO[id].delete()
  }

  override suspend fun get(id: String): Meal = transaction {
    val entity = mealDAO[id]

    mapFromEntity(entity)
  }

  override suspend fun update(meal: Meal): Unit = transaction {
    mealDAO[meal.id]
      .apply {
        title = meal.title
        description = meal.description
        imageUri = meal.image
        lastCookingDate = DateTime.parse(meal.lastCookingDate)
      }
      .refresh(flush = true)
    }

  private fun mapFromEntity(entity: MealEntity) = mealFactory.from(entity)
}

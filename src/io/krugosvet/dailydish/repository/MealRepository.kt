package io.krugosvet.dailydish.repository

import io.krugosvet.dailydish.repository.db.entity.MealDAO
import io.krugosvet.dailydish.repository.db.entity.MealEntity
import io.krugosvet.dailydish.repository.dto.Meal
import io.krugosvet.dailydish.repository.dto.MealFactory
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface MealRepository {
  val meals: List<Meal>

  suspend fun add(meal: Meal): Int

  suspend fun delete(id: Int)

  suspend fun get(id: Int): Meal
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

  override suspend fun add(meal: Meal): Int = transaction {
    mealDAO
      .new {
        title = meal.title
        description = meal.description
        imageUri = meal.image
        lastCookingDate = DateTime.parse(meal.lastCookingDate)
      }
      .id.value
  }

  override suspend fun delete(id: Int): Unit = transaction {
    mealDAO[id].delete()
  }

  override suspend fun get(id: Int): Meal = transaction {
    val entity = mealDAO[id]

    mapFromEntity(entity)
  }

  private fun mapFromEntity(entity: MealEntity) = mealFactory.from(entity)
}
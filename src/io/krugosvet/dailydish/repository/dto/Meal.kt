package io.krugosvet.dailydish.repository.dto

import io.krugosvet.dailydish.config.Config
import io.krugosvet.dailydish.repository.db.entity.MealEntity

data class Meal(
  val title: String,
  val description: String,
  val image: String,
  val lastCookingDate: String
)

class MealFactory(
  private val config: Config
) {

  fun from(entity: MealEntity) =
    Meal(
      title = entity.title,
      description = entity.description,
      image = "http://${config.host}:${config.port}/static/${entity.imageUri}",
      lastCookingDate = entity.lastCookingDate.toString(),
    )
}
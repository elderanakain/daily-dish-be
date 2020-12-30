package io.krugosvet.dailydish.repository.dto

import io.krugosvet.dailydish.repository.db.entity.MealEntity
import kotlinx.serialization.Serializable

@Serializable
data class Meal(
  val title: String,
  val description: String,
  val image: String,
  val lastCookingDate: String
)

class MealFactory {

  fun from(entity: MealEntity) =
    Meal(
      title = entity.title,
      description = entity.description,
      image = entity.imageUri,
      lastCookingDate = entity.lastCookingDate.toString("yyyy-MM-DD"),
    )
}

@file:Suppress("RemoveRedundantQualifierName")

package io.krugosvet.dailydish.repository.dto

import io.krugosvet.dailydish.repository.db.entity.MealEntity
import kotlinx.serialization.Serializable

interface IMeal {
  val title: String
  val description: String
  val image: String?
  val lastCookingDate: String

  fun updateImage(image: String?): IMeal
}

@Serializable
data class Meal(
  val id: String,
  override val title: String,
  override val description: String,
  override val image: String?,
  override val lastCookingDate: String,
) :
  IMeal {

  override fun updateImage(image: String?): Meal = copy(image = image)
}

@Serializable
data class AddMeal(
  override val title: String,
  override val description: String,
  override val lastCookingDate: String,
  override val image: String?
) :
  IMeal {

  override fun updateImage(image: String?): AddMeal = copy(image = image)
}

class MealFactory {

  fun from(entity: MealEntity) =
    Meal(
      id = entity.id.toString(),
      title = entity.title,
      description = entity.description,
      image = entity.imageUri,
      lastCookingDate = entity.lastCookingDate.toString("yyyy-MM-dd"),
    )
}

package io.krugosvet.dailydish.repository.db.entity

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.jodatime.date

object MealTable :
  IdTable<String>() {

  override val id = varchar("id", 100).entityId()

  val title = varchar("title", 1000)
  val description = varchar("description", 1000)
  val imageUri = varchar("image_uri", 1000)

  val lastCookingDate = date("last_cooking_date")
}

class MealDAO :
  EntityClass<String, MealEntity>(MealTable, entityType = MealEntity::class.java)

class MealEntity(id: EntityID<String>) :
  Entity<String>(id) {

  var title by MealTable.title
  var description by MealTable.description
  var imageUri by MealTable.imageUri

  var lastCookingDate by MealTable.lastCookingDate

  override fun toString(): String =
    "Meal(title='$title', description='$description', imageUri='$imageUri', lastCookingDate=$lastCookingDate)"
}

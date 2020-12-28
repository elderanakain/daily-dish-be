package io.krugosvet.dailydish.repository.db.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.date

object MealTable :
  IntIdTable() {

  val title = varchar("title", 100)
  val description = varchar("description", 100)
  val imageUri = varchar("imageUri", 100)

  val lastCookingDate = date("last_cooking_date")
}

class MealDAO :
  IntEntityClass<MealEntity>(MealTable, entityType = MealEntity::class.java)

class MealEntity(id: EntityID<Int>) :
  IntEntity(id) {

  var title by MealTable.title
  var description by MealTable.description
  var imageUri by MealTable.imageUri

  var lastCookingDate by MealTable.lastCookingDate

  override fun toString(): String =
    "Meal(title='$title', description='$description', imageUri='$imageUri', lastCookingDate=$lastCookingDate)"
}
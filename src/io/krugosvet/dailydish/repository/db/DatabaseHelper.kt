package io.krugosvet.dailydish.repository.db

import io.krugosvet.dailydish.config.Config
import io.krugosvet.dailydish.repository.db.entity.MealTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

private const val DRIVER = "org.postgresql.Driver"

class DatabaseHelper(
  private val config: Config,
) {

  fun connect() {
    Database.connect(config.dbUrl, DRIVER, user = config.dbUser, password = config.dbPassword)

    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
      addLogger(StdOutSqlLogger)

      SchemaUtils.create(MealTable)
    }
  }

  fun reset() {
    transaction {
      execInBatch(File("./resources/data/default_db.sql").readLines())
    }
  }
}

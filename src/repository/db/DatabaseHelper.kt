package io.krugosvet.dailydish.repository.db

import io.krugosvet.dailydish.repository.db.entity.MealTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseHelper {

    private const val URL = "jdbc:sqlite:/tmp/data/data.db"
    private const val DRIVER = "org.sqlite.JDBC"

    fun connect() {
        Database.connect(URL, DRIVER)

        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create (MealTable)
        }
    }
}

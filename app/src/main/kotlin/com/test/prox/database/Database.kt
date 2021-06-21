package com.test.prox.database

import io.ktor.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.logTimeSpent
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

var db: Database? = null
val Application.database: Database
    get() = db!!

fun Application.connectDatabase() {
    db = Database.connect(
        url = environment.config.property("db.url").getString(),
        driver = environment.config.property("db.driver").getString(),
        user = environment.config.property("db.user").getString(),
        password = environment.config.property("db.password").getString(),
    )
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users)
//        val createStatements = SchemaUtils.createStatements(Users)
//        println(createStatements)
    }
}
package com.test.prox.database

import io.ktor.application.*
import org.ktorm.database.Database

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
}
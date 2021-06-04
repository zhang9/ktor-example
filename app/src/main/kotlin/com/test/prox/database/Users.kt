package com.test.prox.database

import com.test.prox.entities.User
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object Users : Table<User>("users") {
    val uid = varchar("uid").primaryKey().bindTo { it.uid }
    val name = varchar("name").bindTo { it.name }
    val email = varchar("email").bindTo { it.email }
}

val Database.usersRepo get() = this.sequenceOf(Users)
package com.test.prox.database

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*


object Users : UUIDTable("users") {

    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val age = integer("age").default(18)
}

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var age by Users.age
    fun getResponse(): UserResponse {
        return UserResponse(
            id.value.toString(),
            name,
            email,
            age
        )
    }
}

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val age: Int
)
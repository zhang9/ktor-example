package com.test.prox.entities

import org.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    var uid: String
    var name: String
    var email: String
}
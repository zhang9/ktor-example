package com.test.prox.console

import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.Entity
import org.ktorm.schema.Table


fun <T : Entity<T>> Database.createTable(table: Table<T>) {
    println(table.tableName)
    table.columns
}

class DbAdapter {
    companion object {
        val db = Database.connect("jdbc:mysql://localhost:3306/kotlin_example", driver = "com.mysql.cj.jdbc.Driver",  user = "root", password = "")
        fun init() {
        }
    }
}
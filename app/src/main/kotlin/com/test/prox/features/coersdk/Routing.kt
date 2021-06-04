package com.test.prox.features.coersdk

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun CoreSdk.configureRouting() {
    application.routing {
        get("/auth/login") {
            call.respondText("login page")
        }
        get("/auth/check") {
            call.respondRedirect("/auth/login")
        }
    }
}
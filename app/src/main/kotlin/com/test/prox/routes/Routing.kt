package com.test.prox.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.test.prox.database.database
import com.test.prox.database.usersRepo
import com.test.prox.entities.User
import com.test.prox.features.coersdk.CoreSdkPrincipal
import com.test.prox.features.route
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.util.*

fun Application.configureRouting() {
    routing {
        route<LoginParams> { loginInfo ->
            println("loginInfo====${loginInfo}")
            val coreSdkConfig = environment.config.config("coreSdk")
            val tokenDomain = coreSdkConfig.property("tokenDomain").getString()
            val algorithm = Algorithm.HMAC256(coreSdkConfig.property("appSecret").getString())

            val token = JWT.create()
                .withIssuer(tokenDomain)
                .withClaim("username", loginInfo.username)
                .withArrayClaim("roles", listOf("ROLE_USER", "ROLE_DEV").toTypedArray())
                .sign(algorithm)

            call.respond(mapOf("token" to token))
        }
        authenticate("coreSdk") {
            route<GetUserRoute> { params ->
                val p = call.principal<CoreSdkPrincipal>()
                println("call get user route success and receive roles = ${p?.roles}")
                requireNotNull(call.parameters["name"]) {
                    "name cannot be empty"
                }

                val uid = params.id
                val db = call.application.database
                val user = db.usersRepo.find { it.uid eq uid }
                call.respondText(user.toString())
            }
        }

        route<CreateUserParams> {
            val params = call.receive<CreateUserParams>()
            println("CreateUserParams====${CreateUserParams}")
            val db = call.application.database
            val user = User {
                uid = UUID.randomUUID().toString()
                name = params.name
                email = params.email
            }
            db.usersRepo.add(user)
            call.respond(mapOf("uid" to user.uid, "name" to user.name, "email" to user.email))
        }
    }
}
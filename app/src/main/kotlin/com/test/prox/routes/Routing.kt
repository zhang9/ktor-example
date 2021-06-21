package com.test.prox.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.test.prox.database.User
import com.test.prox.features.coersdk.CoreSdkPrincipal
import com.test.prox.features.route
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
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

                val uid = params.id
                val user = transaction {
                    User.findById(UUID.fromString(uid))
                }
                user?.let {
                    call.respond(it.getResponse())
                }
            }
        }

        route<CreateUserParams> {
            val params = call.receive<CreateUserParams>()
            println("CreateUserParams====${CreateUserParams}")
            val user = transaction {
                User.new {
                    name = params.name
                    email = params.email
                }
            }
            call.respond(user.getResponse())
        }

        route<UpdateUserParams> {
            val params = call.receive<UpdateUserParams>()
            println("UpdateUserParams====${CreateUserParams}")
            val uid = params.id
            transaction {
                User.findById(UUID.fromString(uid))?.also {
                    it.email = params.email
                }
            }?.let {
                call.respond(it.getResponse())
            }
        }
    }
}
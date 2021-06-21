package com.test.prox.routes

import com.test.prox.features.ExtendLocation
import kotlinx.serialization.Serializable

@ExtendLocation("post", "/login")
@Serializable
data class LoginParams(val username: String , val password: String )


@ExtendLocation("post", "/user", "ROLE_ADMIN")
@Serializable
data class CreateUserParams(val name: String, val email: String)

@ExtendLocation("put", "/user/{id}", "ROLE_ADMIN")
@Serializable
data class UpdateUserParams(val id: String, val name: String, val email: String)

@ExtendLocation("get", "/user/{id}", "ROLE_ADMIN")
@Serializable
data class GetUserRoute(val id: String)

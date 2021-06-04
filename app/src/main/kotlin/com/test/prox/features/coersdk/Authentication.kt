package com.test.prox.features.coersdk


import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val CoreSdkAuthKey: Any = "CoreSdkAuth"
private val CoreSdkLogger: Logger = LoggerFactory.getLogger("io.ktor.auth.coreSdk")


public class JwtCredential(val payload: Payload) : Credential
public class CoreSdkPrincipal(val roles: List<String>) : Principal

typealias AuthChallengeFunction = suspend PipelineContext<*, ApplicationCall>.() -> Unit

class CoreSdkAuthenticationProvider(config: Configuration) : AuthenticationProvider(config) {
    private val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(config.appSecret))
        .build()
    private val challenge: AuthChallengeFunction = {
        call.respond(HttpStatusCode.Unauthorized, mapOf("code" to 401))
    }
    private fun getToken(call: ApplicationCall): String? {
        return call.request.headers["access_token"]
    }
    private fun getCredential(token: String): JwtCredential {
        val jwt = jwtVerifier.verify(token)
        val payloadString = String(Base64.getUrlDecoder().decode(jwt.payload))
        return JwtCredential(JWTParser().parsePayload(payloadString))
    }

    private fun getPrincipal(credential: JwtCredential): Principal {
        val receiveRoles = credential.payload.getClaim("roles").asArray(String::class.java).toCollection(ArrayList())
        return CoreSdkPrincipal(receiveRoles)
    }

    init {
        pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
            val token = getToken(call)
            if (token == null) {
                context.extendChallenge(AuthenticationFailedCause.NoCredentials, challenge)
                return@intercept
            }
            CoreSdkLogger.trace("receive token ===== $token")

            val credential = getCredential(token)
            val principal = getPrincipal(credential)
            context.principal(principal)

        }
    }

    class Configuration(config: CoreSdk.Configuration) : AuthenticationProvider.Configuration(name = "coreSdk") {
        val appSecret = config.appSecret
        internal fun build() = CoreSdkAuthenticationProvider(this)
    }

    fun register(authConfiguration: Authentication.Configuration) {
        authConfiguration.register(this)
    }
}

private fun AuthenticationContext.extendChallenge(
    cause: AuthenticationFailedCause,
    challengeFunction: AuthChallengeFunction
) = challenge(CoreSdkAuthKey, cause) {
    challengeFunction(this)
    if (!it.completed && call.response.status() != null) {
        it.complete()
    }
}


fun Authentication.Configuration.coreSdk(config: CoreSdk.Configuration) {
    val provider = CoreSdkAuthenticationProvider.Configuration(config).build()
    provider.register(this)
}


fun CoreSdk.configureSecurity() {
    application.authentication {
        coreSdk(configuration)
    }
}
/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("prox.kotlin-application-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.0"
}

application {
    // Define the main class for the application.
    mainClass.set("io.ktor.server.netty.EngineMain")
}

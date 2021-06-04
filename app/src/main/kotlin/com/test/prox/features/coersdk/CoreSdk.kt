package com.test.prox.features.coersdk

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.util.*

class CoreSdk(val application: Application, val configuration: Configuration){
    init {
        configureRouting()
        configureSecurity()
    }
    class Configuration(config: ApplicationConfig) {
        val a = config.run {  }
        val appId = config.property("appId").getString()
        val appSecret = config.property("appSecret").getString()
    }
    companion object Feature : ApplicationFeature<Application, Configuration, CoreSdk> {
        // Creates a unique key for the feature.
        override val key = AttributeKey<CoreSdk>("CoreSdk")

        // Code to execute when installing the plugin.
        override fun install(pipeline: Application, configure: Configuration.() -> Unit):  CoreSdk {
            val config = pipeline.environment.config.config("coreSdk")
            val configuration = Configuration(config).apply(configure)
            return CoreSdk(pipeline, configuration)
        }
    }
}
package com.test.prox.features

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import java.lang.reflect.Type
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

@Target(AnnotationTarget.CLASS)
annotation class ExtendLocation(val method: String, val path: String, val requiredRole: String = "")

inline fun <reified T : Annotation> KAnnotatedElement.annotation(): T? {
    return annotations.singleOrNull { it.annotationClass == T::class } as T?
}

data class ExtendLocationPropertyInfo(
    val name: String,
    val isOptional: Boolean
)

data class ExtendLocationInfo(
//    val parent: ExtendLocationInfo?,
    val klass: KClass<*>,
    val path: String,
    val method: HttpMethod,
    val requiredRole: String,
    val parameters: List<ExtendLocationPropertyInfo>,
)

open class CustomLocations(private val application: Application, configuration: Configuration) {
    private val roles = configuration.roleHashMap // Copies a snapshot of the mutable config into an immutable property.

    private val conversionService: ConversionService
        get() = application.conversionService

    private fun setRequiredRole(code: Int, role: String) {
        roles[code] = role
    }

    fun getRequiredRole(code: Int): String {
        return roles[code] ?: ""
    }

    fun setRequiredRoleToAll(route: Route, role: String) {
        setRequiredRole(route.hashCode(), role)
        route.children.forEach {
            setRequiredRoleToAll(it, role)
        }
    }

    fun getLocationInfo(locationClass: KClass<*>): ExtendLocationInfo {
        val path = locationClass.annotation<ExtendLocation>()?.path ?: ""
        val method = locationClass.annotation<ExtendLocation>()?.method ?: "GET"
        val requiredRole = locationClass.annotation<ExtendLocation>()?.requiredRole ?: ""

        val constructor: KFunction<Any> = locationClass.primaryConstructor ?: locationClass.constructors.single()
        val pathParameters = RoutingPath.parse(path).parts
            .filter { it.kind == RoutingPathSegmentKind.Parameter }
            .map { PathSegmentSelectorBuilder.parseName(it.value) }

        val parameters = constructor.parameters.filterNot {
            it.name in pathParameters
        }.map { item ->
            ExtendLocationPropertyInfo(item.name!!, item.isOptional)
        }

        return ExtendLocationInfo(locationClass, path, HttpMethod(method.toUpperCase()), requiredRole, parameters)
    }

    inline fun <reified T : Any> resolve(allParameters: Parameters): T {
        val info = getLocationInfo(T::class)
        val klass = info.klass
//        val objectInstance = klass.objectInstance
//        if (objectInstance != null) return objectInstance

        val constructor: KFunction<Any> = klass.primaryConstructor ?: klass.constructors.single()
        val parameters = constructor.parameters
        val arguments = parameters.map { parameter ->
            val parameterType = parameter.type
            val parameterName = parameter.name as String
            val value: Any? =
                createFromParameters(allParameters, parameterName, parameterType.javaType, parameter.isOptional)
            parameter to value
        }.filterNot { it.first.isOptional && it.second == null }.toMap()

        return constructor.callBy(arguments) as T
    }

    fun createFromParameters(parameters: Parameters, name: String, type: Type, optional: Boolean): Any? {
        return when (val values = parameters.getAll(name)) {
            null -> when {
                !optional -> {
                    throw MissingRequestParameterException(name)
                }
                else -> null
            }
            else -> {
                try {
                    conversionService.fromValues(values, type)
                } catch (cause: Throwable) {
                    throw ParameterConversionException(name, type.toString(), cause)
                }
            }
        }
    }

    class Configuration {
        val roleHashMap = hashMapOf<Int, String>()
    }

    // Implements ApplicationFeature as a companion object.
    companion object Feature : ApplicationFeature<Application, Configuration, CustomLocations> {
        // Creates a unique key for the feature.
        override val key = AttributeKey<CustomLocations>("CustomLocations")

        // Code to execute when installing the plugin.
        override fun install(pipeline: Application, configure: Configuration.() -> Unit): CustomLocations {

            val configuration = Configuration().apply(configure)
            return CustomLocations(pipeline, configuration)
        }
    }
}

inline fun <reified T : Any> Route.extendHandle(
    method: HttpMethod,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    if (method.value == HttpMethod.Get.value) {
        println("extendHandle get method")
        var tmp: T? = null
        intercept(ApplicationCallPipeline.Features) {
            println("location resolve ===== ${call.parameters}")
            tmp = customLocations.resolve(call.parameters)
        }
        handle {
            body(tmp as T)
        }
    } else {
        println("extendHandle other method")
        handle {
            body(call.receive())
        }
    }
}

fun Route.getRequiredRole(): String {
    return application.customLocations.getRequiredRole(hashCode())
}

inline fun <reified T : Any> Route.route(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    val info = application.customLocations.getLocationInfo(T::class)
    println("get location info ===== $info")
    val pathRoute = createRouteFromPath(info.path)
    val foldRoute = if (info.method.value == HttpMethod.Get.value) {
        info.parameters.fold(pathRoute) { entry, query ->
            val selector = if (query.isOptional) {
                OptionalParameterRouteSelector(query.name)
            } else {
                //TODO generate post params selector when httpMethod is not get
                ParameterRouteSelector(query.name)
            }
            val route = entry.createChild(selector)
            route
        }
    } else {
        pathRoute
    }
    val route = foldRoute.apply {
        method(info.method) {
            extendHandle(info.method, body)
        }
    }
    application.customLocations.setRequiredRoleToAll(route, info.requiredRole)
    return route
}

val PipelineContext<Unit, ApplicationCall>.customLocations: CustomLocations
    get() = call.application.customLocations

//val ApplicationCall.customLocations: CustomLocations
//    get() = application.customLocations

val Application.customLocations: CustomLocations
    get() = feature(CustomLocations)
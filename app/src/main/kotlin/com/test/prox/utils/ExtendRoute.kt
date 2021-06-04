package com.test.prox.utils


import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


data class PostParameterRouteSelector<T : Any>(
    val name: String,
    val data: KClass<T>
) : RouteSelector(RouteSelectorEvaluation.qualityQueryParameter) {

    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        //TODO get post params
        GlobalScope.launch {
            val tmp = context.call.receiveParameters()
            println("receive post parameters ==== $tmp")
        }
        val param: String = ""
        if (param != null) {
            return RouteSelectorEvaluation(
                true,
                RouteSelectorEvaluation.qualityQueryParameter,
                parametersOf(name, param)
            )
        }
        return RouteSelectorEvaluation.Failed
    }

    override fun toString(): String = "[$name]"
}
//TODO Optional post params selector

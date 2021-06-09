package com.test.prox.utils

import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.util.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor



@OptIn(ExperimentalSerializationApi::class)
class FormUrlEncodeTransform : StringFormat {
    @OptIn(ExperimentalSerializationApi::class)
    override val serializersModule: SerializersModule
        get() = EmptySerializersModule

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val params = parseQueryString(string)
        var tmp = mutableListOf<String>()
        params.forEach { s, _ ->
            tmp.add("\"$s\":\"${params[s]}\"")
        }
        return DefaultJson.decodeFromString(deserializer, tmp.joinToString(prefix ="{", postfix = "}"))
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        TODO("Not yet implemented")
    }

}



@OptIn(ExperimentalSerializationApi::class)
fun ContentNegotiation.Configuration.formUrlEncoded() {
    serialization(ContentType.Application.FormUrlEncoded, FormUrlEncodeTransform() as StringFormat)
}
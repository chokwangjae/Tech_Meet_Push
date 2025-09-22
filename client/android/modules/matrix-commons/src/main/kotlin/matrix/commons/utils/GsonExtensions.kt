package matrix.commons.utils

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * @author tarkarn
 * @since 2024. 11. 13.
 * Gson 을 쉽게 parsing 하기 위한 Extension.
 */

val gson: Gson = GsonBuilder().create()
private val prettyGson: Gson = GsonBuilder().setPrettyPrinting().create()

/**
 * Convert Object to compact Json
 */
fun Any?.toJson(): String {
    return try {
        gson.toJson(this)
    } catch (e: JsonSyntaxException) {
        throw RuntimeException(e)
    }
}

/**
 * Convert List to Json
 */
fun <T> List<T>.toJson(): String {
    return gson.toJson(this)
}

/**
 * Convert Map to Json
 */
fun <K, V> Map<K, V>.toJson(): String {
    return gson.toJson(this)
}

/**
 * Convert Object to pretty Json
 */
fun Any?.toPrettyJson(): String {
    return try {
        prettyGson.toJson(this)
    } catch (e: JsonSyntaxException) {
        throw RuntimeException(e)
    }
}

/**
 * Convert Any to specific type (DataClass, List, Map)
 */
inline fun <reified T> Any?.fromJson(): T = try {
    when (this) {
        is String -> gson.fromJson(this, T::class.java)
        is JsonObject -> gson.fromJson(this, T::class.java)
        is JsonElement -> gson.fromJson(this, T::class.java)
        null -> throw IllegalArgumentException("JSON is null")
        else -> throw IllegalArgumentException("Unsupported JSON type: ${this.javaClass}")
    }
} catch (e: JsonSyntaxException) {
    throw RuntimeException(e)
}

/**
 * Convert Any to List
 */
inline fun <reified T> Any?.fromJsonList(): List<T> = try {
    when (this) {
        is String -> gson.fromJson(this, typeToken<List<T>>())
        is JsonArray -> gson.fromJson(this, typeToken<List<T>>())
        is JsonElement -> gson.fromJson(this, typeToken<List<T>>())
        null -> throw IllegalArgumentException("JSON is null")
        else -> throw IllegalArgumentException("Unsupported JSON type: ${this.javaClass}")
    }
} catch (e: JsonSyntaxException) {
    throw RuntimeException(e)
}

/**
 * Convert Any to Map
 */
inline fun <reified K, reified V> Any?.fromJsonMap(): Map<K, V> = try {
    when (this) {
        is String -> gson.fromJson(this, typeToken<Map<K, V>>())
        is JsonObject -> gson.fromJson(this, typeToken<Map<K, V>>())
        is JsonElement -> gson.fromJson(this, typeToken<Map<K, V>>())
        null -> throw IllegalArgumentException("JSON is null")
        else -> throw IllegalArgumentException("Unsupported JSON type: ${this.javaClass}")
    }
} catch (e: JsonSyntaxException) {
    throw RuntimeException(e)
}

inline fun <reified T> Any?.fromJsonOrNull(): T? = try {
    this?.fromJson<T>()
} catch (e: Exception) {
    null
}

/**
 * Convert to Json and encode the resulting UTF-8 bytes as a Base64 string
 */
fun Any?.encodeToBase64(): String {
    return try {
        val json = gson.toJson(this)
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: JsonSyntaxException) {
        throw RuntimeException(e)
    }
}

/**
 * Decode from Base64 json string into required type
 */
fun <T> String?.decodeFromBase64(target: Class<T>): T {
    return try {
        val bytes = Base64.decode(this, Base64.DEFAULT)
        val json = String(bytes, StandardCharsets.UTF_8)
        gson.fromJson(json, target)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

/**
 * Decode from Base64 json string into required type with Type
 */
fun <T> String?.decodeFromBase64(type: Type): T {
    return try {
        val bytes = Base64.decode(this, Base64.DEFAULT)
        val json = String(bytes, StandardCharsets.UTF_8)
        gson.fromJson(json, type)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

inline fun <reified T> typeToken(): Type = object : TypeToken<T>() {}.type

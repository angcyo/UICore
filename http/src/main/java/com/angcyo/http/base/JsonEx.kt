package com.angcyo.http.base

import android.text.TextUtils
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="快速构建 jsonObject 和 jsonArray ">

fun jsonBuilder() = JsonBuilder().json()

fun jsonArrayBuilder() = JsonBuilder().array()

fun jsonString(config: JsonBuilder.() -> Unit = {}): String {
    return JsonBuilder().json().run {
        config()
        get()
    }
}

fun arrayString(config: JsonBuilder.() -> Unit = {}): String {
    return JsonBuilder().array().run {
        config()
        get()
    }
}

fun jsonObject(config: JsonBuilder.() -> Unit = {}): JsonElement {
    return JsonBuilder().json().run {
        config()
        build()
    }
}

fun jsonArray(config: JsonBuilder.() -> Unit = {}): JsonElement {
    return JsonBuilder().array().run {
        config()
        build()
    }
}

//</editor-fold desc="快速构建 jsonObject 和 jsonArray ">

//<editor-fold desc="JsonObject 扩展">

fun JsonObject.getInt(key: String, default: Int = -1): Int {
    val element = get(key)
    if (element is JsonPrimitive) {
        if (element.isNumber) {
            return element.asInt
        }
    }
    return default
}

fun JsonObject.getString(key: String, default: String? = null): String? {
    val element = get(key)
    if (element is JsonPrimitive) {
        if (element.isString) {
            return element.asString
        }
    }
    return default
}

fun JsonObject.getJson(key: String, default: JsonObject? = null): JsonObject? {
    val element = get(key)
    if (element is JsonObject) {
        return element
    }
    return default
}

fun JsonObject.getArray(key: String, default: JsonArray? = null): JsonArray? {
    val element = get(key)
    if (element is JsonArray) {
        return element
    }
    return default
}

fun JsonObject.getLong(key: String, default: Long = -1): Long {
    val element = get(key)
    if (element is JsonPrimitive) {
        if (element.isNumber) {
            return element.asLong
        }
    }
    return default
}

fun JsonObject.getDouble(key: String, default: Double = -1.0): Double {
    val element = get(key)
    if (element is JsonPrimitive) {
        if (element.isNumber) {
            return element.asDouble
        }
    }
    return default
}

//</editor-fold desc="JsonObject">

//<editor-fold desc="JsonElement 扩展">

fun JsonElement.getInt(key: String): Int {
    if (this is JsonObject) {
        return this.getInt(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.getString(key: String): String? {
    if (this is JsonObject) {
        return this.getString(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.getJson(key: String): JsonObject? {
    if (this is JsonObject) {
        return this.getJson(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.getArray(key: String): JsonArray? {
    if (this is JsonObject) {
        return this.getArray(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.getLong(key: String): Long {
    if (this is JsonObject) {
        return this.getLong(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.getDouble(key: String): Double {
    if (this is JsonObject) {
        return this.getDouble(key)
    }
    throw IllegalAccessException("不允许使用[JsonArray]操作.")
}

fun JsonElement.array(index: Int): JsonElement {
    if (this is JsonArray) {
        return get(index)
    }
    throw IllegalAccessException("不允许使用[JsonObject]操作.")
}

fun String?.isJsonEmpty(): Boolean {
    return TextUtils.isEmpty(this) || this == "{}" || this == "[]"
}

fun CharSequence?.jsonOrEmpty(): String {
    return if (TextUtils.isEmpty(this)) {
        "{}"
    } else {
        this.toString()
    }
}

fun CharSequence?.isJson(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }

    val char = this!!

    if (char.startsWith("{") && char.endsWith("}")) {
        return true
    }

    if (char.startsWith("[") && char.endsWith("]")) {
        return true
    }

    return false
}

fun CharSequence?.isJsonObject(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }

    val char = this!!

    if (char.startsWith("{") && char.endsWith("}")) {
        return true
    }

    return false
}

fun CharSequence?.isJsonArray(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }

    val char = this!!

    if (char.startsWith("[") && char.endsWith("]")) {
        return true
    }

    return false
}

//</editor-fold desc="JsonElement 扩展">

fun String?.trim(char: Char): String? {
    return this?.trimStart(char)?.trimEnd(char)
}

fun String?.json(): JsonObject? {
    if (TextUtils.isEmpty(this)) {
        return null
    }
    var fromJson: JsonObject? = null
    try {
        fromJson = this?.fromJson(JsonObject::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fromJson
}

fun String?.jsonArray(): JsonArray? {
    if (TextUtils.isEmpty(this)) {
        return null
    }
    var fromJson: JsonArray? = null
    try {
        fromJson = this?.fromJson(JsonArray::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fromJson
}

//<editor-fold desc="Json 解析">

fun GsonBuilder.default(): GsonBuilder {
    setDateFormat("yyyy-MM-dd HH:mm:ss")
    setLenient() //支持畸形json解析
    excludeFieldsWithModifiers(
        Modifier.STATIC,
        Modifier.TRANSIENT,
        Modifier.VOLATILE
    )
    return this
}

fun gson(config: (GsonBuilder.() -> Unit)? = null): Gson {
    val gson = if (config == null) {
        GsonBuilder()
            .default()
            .setPrettyPrinting()
            .serializeNulls()
            //.disableHtmlEscaping() //关闭html转义
            .create()
    } else {
        GsonBuilder()
            .apply(config)
            .create()
    }
    return gson
}

/**任意对象, 转成json字符串*/
fun Any?.toJson(config: (GsonBuilder.() -> Unit)? = null): String? {
    return this?.run {
        try {
            gson(config).toJson(this)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**json字符串, 转成指定对象.
 * 注意, 如果json格式有问题,会返回null, 异常会被捕获
 * */
fun <T> String?.fromJson(typeOfT: Type): T? {
    return this?.run {
        try {
            gson().fromJson<T>(this, typeOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**json字符串, 转成指定对象*/
fun <T> String?.fromJson(classOfT: Class<T>): T? {
    return this?.run {
        try {
            gson().fromJson<T>(this, classOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

inline fun <reified T> String?.fromJson(): T? = this?.fromJson(T::class.java)

inline fun <reified T> String?.fromJson2(): T? =
    gson().fromJson<T>(this, object : TypeToken<T>() {}.type)

//</editor-fold desc="Json 解析">
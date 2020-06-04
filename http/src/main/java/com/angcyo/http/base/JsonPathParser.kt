package com.angcyo.http.base

import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken.*
import com.google.gson.stream.JsonWriter
import java.io.Closeable
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal


/**
 *
 *  Email:angcyo@126.com
 * @author AItsuki
 * @date 2019/8/23.
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object JsonPathParser {

    /**
     * 遍历读取json数据
     *
     * [jsonPath] $.store.book[0].title
     *
     * https://support.smartbear.com/alertsite/docs/monitors/api/endpoint/jsonpath.html
     *
     * @return 使用jsonPath作为key, 文件路径作为value。
     */
    fun read(json: String, need: (jsonPath: String, value: String) -> Unit) {
        val reader = JsonReader(StringReader(json))
        read(reader, need)
        reader.safeClose()
    }

    /**
     * 将字符串写入到path中（使用Gson遍历的方式）
     * @return 返回修改后的json
     */
    fun write(json: String, pathMap: MutableMap<String, String>): String {
        val reader = JsonReader(StringReader(json))
        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        write(reader, writer, pathMap)
        val resultJson = stringWriter.toString()
        reader.safeClose()
        writer.safeClose()
        return resultJson
    }

    private fun read(reader: JsonReader, check: (path: String, value: String) -> Unit) {
        while (true) {
            when (reader.peek()) {
                BEGIN_ARRAY -> reader.beginArray()
                END_ARRAY -> reader.endArray()
                BEGIN_OBJECT -> reader.beginObject()
                END_OBJECT -> reader.endObject()
                NAME -> reader.nextName()
                STRING -> {
                    val value = reader.nextString()
                    val jsonPath = reader.path

                    check(jsonPath, value)
                }
                NUMBER -> reader.nextDouble()
                BOOLEAN -> reader.nextBoolean()
                NULL -> reader.nextNull()
                END_DOCUMENT -> return
                else -> throw JsonParseException("解析json出现了问题，请检查json的合法性。")
            }
        }
    }

    private fun write(reader: JsonReader, writer: JsonWriter, pathMap: MutableMap<String, String>) {
        while (true) {
            when (reader.peek()) {
                BEGIN_ARRAY -> {
                    reader.beginArray()
                    writer.beginArray()
                }
                END_ARRAY -> {
                    reader.endArray()
                    writer.endArray()
                }
                BEGIN_OBJECT -> {
                    reader.beginObject()
                    writer.beginObject()
                }
                END_OBJECT -> {
                    reader.endObject()
                    writer.endObject()
                }
                NAME -> {
                    val name = reader.nextName()
                    writer.name(name)
                }
                STRING -> {
                    val jsonPath = reader.path
                    val s = reader.nextString()
                    if (pathMap.containsKey(jsonPath)) {
                        writer.value(pathMap[jsonPath])
                    } else {
                        writer.value(s)
                    }
                }
                NUMBER -> {
                    val n = reader.nextString()
                    writer.value(BigDecimal(n))
                }
                BOOLEAN -> {
                    val b = reader.nextBoolean()
                    writer.value(b)
                }
                NULL -> {
                    reader.nextNull()
                    writer.nullValue()
                }
                END_DOCUMENT -> return
                else -> throw JsonParseException("解析json出现了问题，请检查json的合法性。")
            }
        }
    }

    private fun Closeable.safeClose() {
        try {
            this.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
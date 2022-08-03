package com.angcyo.http.base

import com.angcyo.library.model.Page
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import java.io.EOFException
import java.lang.reflect.Type
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * https://mvnrepository.com/artifact/com.squareup.okhttp3/logging-interceptor/4.9.2
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**网络请求回调*/
typealias HttpCallback<T> = (data: T?, error: Throwable?) -> Unit

/**读取ResponseBody中的字符串*/
fun ResponseBody?.readString(urlDecode: Boolean = false, charsetName: String = "UTF-8"): String? {
    if (this == null) {
        return null
    }

//    return Buffer().use { buffer ->
//        val source = source()
//        source.read(buffer, Long.MAX_VALUE)
//        val charset: Charset = Charset.forName(charsetName)
//        buffer.clone().readString(source.readBomAsCharset(charset))
//    }

    val source = source()
    source.request(Long.MAX_VALUE)
    val buffer = source.buffer
    val charset: Charset = Charset.forName(charsetName)
    val readString = buffer.clone().readString(charset)
    return if (urlDecode) {
        URLDecoder.decode(readString, charsetName)
    } else {
        readString
    }
}


/**读取RequestBody中的字符串*/
fun RequestBody?.readString(charsetName: String = "UTF-8"): String {
    if (this == null) {
        return ""
    }
    return Buffer().use {
        writeTo(it)
        val charset: Charset = Charset.forName(charsetName)
        it.clone().readString(charset)
    }
}

/**是否是明文*/
fun Buffer.isPlaintext(): Boolean {
    return try {
        val prefix = Buffer()
        val byteCount = if (buffer.size < 64) buffer.size else 64
        buffer.copyTo(prefix, 0, byteCount)
        for (i in 0..15) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) &&
                !Character.isWhitespace(codePoint)
            ) {
                return false
            }
        }
        true
    } catch (e: EOFException) {
        false // Truncated UTF-8 sequence.
    }
}

/**[String]组装数据成[HashMap]*/
fun mapOf(vararg args: String, split: String = ":"): HashMap<String, Any> {
    val result = hashMapOf<String, Any>()

    args.forEach {
        if (it.isNotEmpty()) {
            val splitArray = it.split(split)
            if (splitArray.size == 2) {
                val key = splitArray[0]
                val value = splitArray[1]
                result[key] = value
            }
        }
    }

    return result
}

/**application/json*/
fun String.toJsonBody(): RequestBody =
    toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

fun String?.isJsonType() = this?.startsWith("application/json") == true
fun String?.isTextType() = this?.startsWith("text/plain") == true
fun String?.isHtmlType() = this?.startsWith("text/html") == true

fun MediaType?.isJsonType() = this?.toString().isJsonType()
fun MediaType?.isTextType() = this?.toString().isTextType()
fun MediaType?.isHtmlType() = this?.toString().isHtmlType()

/**快速设置[page]参数*/
fun JsonBuilder.addPage(page: Page?) {
    if (page != null) {
        if (page.requestPageSize < 0) {
            add(page.keySize, Int.MAX_VALUE)
            add(page.keyCurrent, 1)
        } else {
            add(page.keySize, page.requestPageSize)
            add(page.keyCurrent, page.requestPageIndex)
        }
    }
}

/**
 * HttpBean<Bean>
 * */
fun httpBeanType(wrapClass: Class<*>, typeClass: Class<*>): Type =
    type(wrapClass, typeClass)

/**
 * HttpBean<List<Bean>>
 * */
fun httpListBeanType(wrapClass: Class<*>, typeClass: Class<*>): Type =
    type(wrapClass, type(List::class.java, typeClass))

/**成功*/
fun Int.isSuccess() = this in 200..299
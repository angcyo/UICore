package com.angcyo.http.base

import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import java.io.EOFException
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**读取ResponseBody中的字符串*/
fun ResponseBody?.readString(urlDecode: Boolean = true, charsetName: String = "UTF-8"): String {
    if (this == null) {
        return ""
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

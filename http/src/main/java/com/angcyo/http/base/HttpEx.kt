package com.angcyo.http.base

import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import java.nio.charset.Charset

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**读取ResponseBody中的字符串*/
fun ResponseBody?.readString(charsetName: String = "UTF-8"): String {
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
    return buffer.clone().readString(charset)
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

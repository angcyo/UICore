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
    val buffer = Buffer()
    writeTo(buffer)
    val charset: Charset = Charset.forName(charsetName)
    return buffer.clone().readString(charset)
}

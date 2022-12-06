package com.angcyo.library.ex

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

fun InputStream.toText(charset: Charset = Charsets.UTF_8) = readText(charset)

fun InputStream.readText(charset: Charset = Charsets.UTF_8) = readBytes().toString(charset)

/**将输入流写到输出流
 * [outputStream]
 * [bufferSize]*/
fun InputStream.writeTo(outputStream: OutputStream, bufferSize: Int = 4096) {
    var len = 0
    val buffer = ByteArray(bufferSize)
    while ((read(buffer).also { len = it }) != -1) {
        outputStream.write(buffer, 0, len)
    }
}

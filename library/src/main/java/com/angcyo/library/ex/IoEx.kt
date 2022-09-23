package com.angcyo.library.ex

import java.io.InputStream
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
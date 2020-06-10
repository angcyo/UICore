package com.angcyo.library.ex

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

private val hexDigits: CharArray =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

/**加密字节数据
 * [algorithm] 加密算法 MD2/MD5/SHA1/SHA224/SHA256/SHA384/SHA512
 * */
fun ByteArray.encrypt(algorithm: String = "MD5"): ByteArray? {
    return try {
        val md = MessageDigest.getInstance(algorithm)
        md.update(this)
        md.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        null
    }
}

/**
 * byteArr转hexString
 *
 * 例如：
 * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
 *
 * @param bytes 字节数组
 * @return 16进制大写字符串
 */
fun ByteArray.toHexString(): String? {
    val len = size
    if (len <= 0) return null
    val ret = CharArray(len shl 1)
    var i = 0
    var j = 0
    while (i < len) {
        ret[j++] = hexDigits[this[i].toInt().ushr(4) and 0x0f]
        ret[j++] = hexDigits[(this[i] and 0x0f).toInt()]
        i++
    }
    return String(ret)
}

/**格式化十六进制, 两个字符之间加:号
 * 00A8 -> 00:A8
 * */
fun String.beautifyHex(fm: String = ":$0") = replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), fm)
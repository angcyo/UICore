package com.angcyo.library.ex

import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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
 * [algorithm] 加密算法 MD2/MD5/SHA-1/SHA-224/SHA-256/SHA-384/SHA-512
 *
 * 命名规则:
 * https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html
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

/**https://stackoverflow.com/questions/6026339/how-to-generate-hmac-sha1-signature-in-android*/
fun ByteArray.hmacSha1(key: String, type: String = "HmacSHA1"): ByteArray? {
    // 生成HmacSHA1专属密钥
    val secretKey = SecretKeySpec(key.toByteArray(), type)
    // 生成一个指定 Mac 算法 的 Mac 对象
    val mac = Mac.getInstance(type)
    // 用给定密钥初始化 Mac 对象
    mac.init(secretKey)
    return mac.doFinal(this)
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

fun String.toHexString(): String? = toByteArray(Charsets.UTF_8).toHexString()

/**格式化十六进制, 两个字符之间加:号
 * 00A8 -> 00:A8
 *
 * [" $0"]2个字符之间加空格
 * */
fun String.beautifyHex(fm: String = ":$0") = replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), fm)

/**获取字节数组的md5值*/
fun ByteArray.md5() = encrypt()?.toHexString()

/**获取字节数组的hash值*/
fun ByteArray.sha256() = encrypt("SHA-256")?.toHexString()

//---

/**取高8位的值,高位*/
fun Int.high8Bit(): Int = this and 0xff00 shr 8

/**取低8位的值,低位*/
fun Int.low8Bit(): Int = this and 0x00ff

/**高4位*/
fun Byte.high4Bit(): Byte = ((this.toInt() and 0xf0) shr 4).toByte()

/**低4位*/
fun Byte.low4Bit(): Byte = (this.toInt() and 0x0f).toByte()

/**转换成UTF8文本*/
inline fun ByteArray.toText(charset: Charset = Charset.defaultCharset()) = toString(charset)
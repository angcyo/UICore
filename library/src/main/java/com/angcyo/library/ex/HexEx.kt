package com.angcyo.library.ex

import java.util.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/22
 */

/**
 * 将十六进制字符串[0123456789ABCDEF]转成字节数组[ByteArray]
 * 字符串需要是偶数
 * */
fun String.toHexByteArray(): ByteArray {
    val s = replace(" ", "")
    val bs = ByteArray(s.length / 2)
    for (i in 0 until s.length / 2) {
        bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return bs
}

/**删除所有空字符*/
fun String.trimAllChar(char: Char = ' ') = trim(char)

/**将十六进制字符串[0123456789ABCDEF]转换成字节
 * 字节范围[-128~127]*/
fun String.toHexByte(): Byte {
    val s = replace(" ", "")
    return s.toInt(16).toByte()
}

/**
 * 将十六进制字符串[0123456789ABCDEF]补齐到指定的字节数组长度
 * [padEnd] 往后垫, 否则就是往前垫
 * */
fun String.padHexString(length: Int, padEnd: Boolean = true): String {
    val bytes = toHexByteArray().padHexByteArray(length, padEnd)
    return bytes.toHexString(contains(' '))
}

/**将字节数组复制到新的数组中
 * [fromStartPos]开始字节数组开始的位置
 * [targetStartPos]目标字节数组开始的位置
 * [count]复制的数量
 * */
fun ByteArray.copyTo(
    target: ByteArray,
    fromStartPos: Int,
    targetStartPos: Int = 0,
    count: Int = target.size
) {
    System.arraycopy(this, fromStartPos, target, targetStartPos, count)
}

fun ByteArray.padHexByteArray(length: Int, padEnd: Boolean = true): ByteArray {
    val bytes = this

    if (bytes.size >= length) {
        return bytes
    } else {
        val result = ByteArray(length)
        val count = length - bytes.size
        if (padEnd) {
            System.arraycopy(bytes, 0, result, 0, bytes.size)
            for (i in 0 until count) {
                result[bytes.size + i] = 0
            }
        } else {
            System.arraycopy(bytes, 0, result, count, bytes.size)
            for (i in 0 until count) {
                result[i] = 0
            }
        }
        return result
    }
}

/**将字节数组[ByteArray]转换成十六进制字符串[01 23 45 67 89 AB CD EF ]
 * [hasSpace] 是否包含空格*/
fun ByteArray.toHexString(hasSpace: Boolean = true) = joinToString("") {
    it.toHexString() + if (hasSpace) " " else ""
}.trimEnd(' ')

/**将字节数据转成大写十六进制字符
 * [-86, -69, 19, 0, 6, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 1, 0, 1, 12]
 *   AA   BB  13 00 06  FF 00 00 00 00 00 00 00 00 00 00 06 00 01 00 01 0C
 * [length] 需要输出多少个字符, 不足前面补充0*/
fun Byte.toHexString(length: Int = 2, padChar: Char = '0') = toHexInt().toHexString(length, padChar)

fun Int.toHexString(length: Int = 2, padChar: Char = '0') =
    toString(16).padStart(length, padChar).uppercase(Locale.ROOT)

/**将字节转换成无符号整型*/
fun Byte.toHexInt() = toInt() and 0xFF


/**将十六进制字符串转换成字节数字
 * AA -> -86
 * 必须是16进制字符
 * */
fun String.toByte(): Byte = toInt(16).toByte()

/**
 * 获取文件编码类型
 * implementation 'com.googlecode.juniversalchardet:juniversalchardet:1.0.3'
 * @param bytes 文件bytes数组
 * @return      编码类型
 */
/*
fun ByteArray.toEncodeString(): String {
    val defaultEncoding = "UTF-8"
    val detector = UniversalDetector(object : CharsetListener {
        override fun report(charset: String?) {
            Log.e("TAG", "report: $charset")
        }
    })
    detector.handleData(this, 0, this.size)
    detector.dataEnd()
    val encoding = detector.detectedCharset
    detector.reset()

    return when {
        encoding == defaultEncoding -> {
            String(this, Charset.forName(encoding))
        }
        this[0] in 0..127 -> {
            String(this)
        }
        else -> {
            ""
        }
    }
}
*/

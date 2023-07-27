package com.angcyo.library.ex

import com.angcyo.library.L
import java.util.*
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/22
 */

/**
 * 将十六进制字符串[0123456789ABCDEF]转成字节数组[ByteArray]
 * //0204 0-23 00-A 000401
 * 字符串需要是偶数
 * */
fun String.toHexByteArray(): ByteArray {
    val s = replace(" ", "")
    val bs = ByteArray(s.length / 2)
    try {
        for (i in 0 until s.length / 2) {
            bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    } catch (e: Exception) {
        L.w("无法转换:$this")
        throw e
    }
    return bs
}

/**删除前尾所有空字符*/
fun String.trimAllChar(char: Char = ' ') = trim(char)

/**移除所有字符*/
fun String.removeAll(char: String = " ") = replace(char, "", true)

/**将十六进制字符串[0123456789ABCDEF]转换成字节
 * 字节范围[-128~127]*/
fun String.toHexByte(): Byte {
    val s = replace(" ", "")
    return s.toInt(16).toByte()
}

/**
 * 尾部补齐, 或者首部补齐
 * 将十六进制字符串[0123456789ABCDEF]补齐到指定的字节数组长度
 * [pad] 补齐的方向, 负数:往后补齐, 正数:往前补齐, 0:不补齐
 * */
fun String.padHexString(length: Int, pad: Int = -1): String {
    val bytes = toHexByteArray().padHexByteArray(length, pad)
    return bytes.toHexString(contains(' '))
}

/**将字节数组复制到新的数组中
 * [fromStartPos]开始字节数组开始的位置
 * [targetStartPos]目标字节数组开始的位置
 * [count]复制的数量
 * */
fun ByteArray.copyTo(
    target: ByteArray,
    fromStartPos: Int = 0,
    targetStartPos: Int = 0,
    count: Int = target.size
) {
    System.arraycopy(this, fromStartPos, target, targetStartPos, count)
}

/**剔除字节数据, 或者填满字节数组到指定的长度
 * [length] 限定的字节数量
 * [pad] 补齐的方向, 负数:往后补齐, 正数:往前补齐, 0:不补齐
 * */
fun ByteArray.trimAndPad(length: Int, pad: Int = -1): ByteArray {
    return if (size == length) {
        //刚好相等
        this
    } else if (size > length) {
        //需要剔除
        val result = ByteArray(length)
        copyTo(result, count = length)
        result
    } else {
        padHexByteArray(length, pad)
    }
}

/**填满字节数组到指定的长度[length], 如果已经超过则忽略
 * [pad] 补齐的方向, 负数:往后补齐, 正数:往前补齐, 0:不补齐
 * */
fun ByteArray.padHexByteArray(length: Int, pad: Int = -1): ByteArray {
    val bytes = this
    if (bytes.size >= length) {
        return bytes
    } else {
        val result = ByteArray(length)
        val count = length - bytes.size
        if (pad < 0) {
            //往后填充
            System.arraycopy(bytes, 0, result, 0, bytes.size)
            for (i in 0 until count) {
                result[bytes.size + i] = 0
            }
        } else if (pad > 0) {
            //往前填充
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

/**整型转成十六进制字符串, (负数转16进制会出事)
 * 2个十六进制字符表示1个字节 8位
 * [length] 十六进制字符串的长度, 除以2 就表示字节的大小
 * [padChar] 需要补齐的字符
 * */
fun Int.toHexString(length: Int = 2, padChar: Char = '0') =
    absoluteValue.toString(16).padStart(length, padChar).uppercase(Locale.ROOT)

/**[ByteArray]
 * [length] 需要多少个字节, 1个字节8位. 不够前面补0
 * [com.angcyo.library.ex.toByteInt]
 * https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
 * */
fun Int.toByteArray(length: Int): ByteArray {
    //ByteBuffer.allocate(capacity).putInt(this).array()
    val result = ByteArray(length)
    for (index in 0 until length) {
        result[length - index - 1] = (this shr (index * 8) and 0xff).toByte()
    }
    return result
}

/**将字节数组,按照对应的位转成Int类型
 * [com.angcyo.library.ex.toByteArray]
 * [kotlin.ByteArray.toHexInt]
 * [kotlin.ByteArray.toByteInt]
 * */
fun ByteArray.toByteInt(): Int {
    var result = 0
    for (index in 0 until size) {
        val byte = get(index).toInt() and 0xff
        result = result or (byte shl (size - index - 1) * 8)
    }
    return result
}

/**将字节转换成无符号整型
 * -1 -> 255
 * */
fun Byte.toHexInt() = toInt() and 0xFF

/**
 * 将字节数组[-86, -69]转换成对应的整型数字[43707]
 * FFFF -> 65535
 * [kotlin.ByteArray.toHexInt]
 * [kotlin.ByteArray.toByteInt]
 * */
fun ByteArray.toHexInt() = toHexString(false).toHexInt()

/**将十六进制字符[0101]转换成整型数组*/
fun String.toHexInt() = if (this.isEmpty()) {
    -1
} else {
    try {
        //最大值 FFFFFFFF 会崩溃
        toInt(16)
    } catch (e: Exception) {
        -1
    }
}

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

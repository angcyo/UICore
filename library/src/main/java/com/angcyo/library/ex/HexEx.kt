package com.angcyo.library.ex

import android.util.Log
import java.nio.charset.Charset
import java.util.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/22
 */

/**
 * 将十六进制字符串[0123456789ABCDEF]转成字节数组[ByteArray]
 * */
fun String.toHexByteArray(): ByteArray {
    val s = replace(" ", "")
    val bs = ByteArray(s.length / 2)
    for (i in 0 until s.length / 2) {
        bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return bs
}

/**将字节数组[ByteArray]转换成十六进制字符串[01 23 45 67 89 AB CD EF ]
 * [hasSpace] 是否包含空格*/
fun ByteArray.toHexString(hasSpace: Boolean = true) = joinToString("") {
    (it.toInt() and 0xFF).toString(16).padStart(2, '0')
        .uppercase(Locale.ROOT) + if (hasSpace) " " else ""
}



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

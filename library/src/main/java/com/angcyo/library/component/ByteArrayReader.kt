package com.angcyo.library.component

import com.angcyo.library.ex.copyTo
import com.angcyo.library.ex.toHexInt
import java.nio.charset.Charset

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/25
 */
class ByteArrayReader(val bytes: ByteArray) {

    var _index = 0

    fun offset(size: Int) {
        _index += size
    }

    /**在指针位置读取[size]个字节*/
    fun read(size: Int): ByteArray {
        if (_index + size > bytes.size) {
            //超出范围
            return byteArrayOf()
        }
        val result = ByteArray(size)
        bytes.copyTo(result, _index)
        offset(size)
        return result
    }

    /**超范围时, 返回-1*/
    fun readInt(size: Int): Int {
        val array = read(size)
        if (array.isEmpty()) {
            return -1
        }
        return array.toHexInt()
    }

    /**超范围时, 返回null*/
    fun readString(size: Int, charset: Charset = Charsets.UTF_8): String? {
        val array = read(size)
        if (array.isEmpty()) {
            return null
        }
        return String(array, charset)
    }

}

fun ByteArray.reader(action: ByteArrayReader.() -> Unit): ByteArrayReader {
    return ByteArrayReader(this).apply {
        action()
    }
}
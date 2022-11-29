package com.angcyo.library.component

import com.angcyo.library.ex.copyTo
import com.angcyo.library.ex.toByteInt
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

    /**在指针位置读取[size]个字节, 并且自动偏移滞后*/
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

    /** 超范围时, 返回-1*/
    fun readByte(def: Byte = -1): Byte {
        val array = read(1)
        if (array.isEmpty()) {
            return def
        }
        return array.first()
    }

    /**
     * 读取多少个字节表示的int数据
     * 超范围时, 返回-1*/
    fun readInt(size: Int, def: Int = -1): Int {
        val array = read(size)
        if (array.isEmpty()) {
            return def
        }
        return array.toHexInt()
    }

    fun readByteInt(size: Int, def: Int = -1): Int {
        val array = read(size)
        if (array.isEmpty()) {
            return def
        }
        return array.toByteInt()
    }

    /**读取多少个字节表示的string数据
     * 超范围时, 返回null*/
    fun readString(size: Int, charset: Charset = Charsets.UTF_8, def: String? = null): String? {
        val array = read(size)
        if (array.isEmpty()) {
            return def
        }
        return String(array, charset)
    }
}

/**字节读取器,自动偏移*/
fun ByteArray.reader(action: ByteArrayReader.() -> Unit): ByteArrayReader {
    return ByteArrayReader(this).apply {
        action()
    }
}
package com.angcyo.library.component

import com.angcyo.library.ex.copyTo
import com.angcyo.library.ex.toByteInt
import com.angcyo.library.ex.toHexInt
import com.angcyo.library.ex.toText
import java.nio.charset.Charset

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/25
 */
class ByteArrayReader(val bytes: ByteArray) {

    /**需要保留最后几个字节*/
    var keepLastSize: Int = 0

    var _index = 0

    val byteSize: Int
        get() = bytes.size - keepLastSize

    /**偏移多少个字节*/
    fun offset(size: Int) {
        _index += size
    }

    /**偏移并且返回指定的值*/
    fun <T> offset(size: Int, result: T): T {
        offset(size)
        return result
    }

    /**确保剩下的字节数大于指定的值*/
    fun assertLast(length: Int) {
        assert(_index + length <= bytes.size)
    }

    /**在指针位置读取[size]个字节, 并且自动偏移滞后*/
    fun read(size: Int): ByteArray {
        if (_index + size > byteSize || size <= 0) {
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

    /**读取字符串直到0x00结束*/
    fun readStringEnd(def: String? = null, charset: Charset = Charsets.UTF_8): String? {
        val bytes = readLoop { _, byte ->
            byte.toUByte().toInt() == 0
        }
        if (bytes.isEmpty()) {
            return def
        }
        return String(bytes, charset)
    }

    /**循环读取连续的字符串
     * [maxSize] 需要读取的最大字节数*/
    fun readStringList(maxSize: Int, charset: Charset = Charsets.UTF_8): List<String> {
        val result = mutableListOf<String>()
        var count = 0
        while (true) {
            val bytes = readLoop { _, byte ->
                byte.toUByte().toInt() == 0
            }
            if (bytes.isEmpty()) {
                //超出范围
                break
            }
            result.add(bytes.toText(charset))
            count += bytes.size
            if (count >= maxSize) {
                //超出范围
                break
            }
        }
        return result
    }

    /**循环读取, 直到停止状态
     * [predicate] 返回true, 停止读取
     * */
    fun readLoop(predicate: (byteList: List<Byte>, byte: Byte) -> Boolean): ByteArray {
        val result = mutableListOf<Byte>()
        while (true) {
            val byte = readByte()
            if (byte.toInt() == -1) {
                //超出范围
                break
            }
            if (predicate(result, byte)) {
                break
            }
            result.add(byte)
        }
        return result.toByteArray()
    }
}

/**字节读取器,自动偏移*/
fun ByteArray.reader(action: ByteArrayReader.() -> Unit): ByteArrayReader {
    return ByteArrayReader(this).apply {
        action()
    }
}
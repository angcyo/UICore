package com.angcyo.library.component

import com.angcyo.library.ex.toByteArray
import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/31
 */
class ByteArrayWriter(
    /**初始化的大小*/
    initialSize: Int = 32,
    /**限制最大写入的大小, 负数不限制*/
    val limitMaxSize: Int = -1
) {

    val outputStream: ByteArrayOutputStream = ByteArrayOutputStream(initialSize)

    fun toByteArray() = outputStream.toByteArray()

    /**
     * [int] 整数
     * [length] 字节数, 不够前面补0
     * */
    fun write(int: Int, length: Int) {
        if (limitMaxSize >= 0 && outputStream.size() >= limitMaxSize) {
            return
        } else {
            val size = if (limitMaxSize >= 0) {
                min(length, limitMaxSize - outputStream.size())
            } else {
                length
            }
            outputStream.write(int.toByteArray(size))
        }
    }

    /**[byte] 只写入1个字节*/
    fun write(byte: Int) {
        if (limitMaxSize >= 0 && outputStream.size() >= limitMaxSize) {
            return
        }
        outputStream.write(byte)
    }

    fun write(byte: Byte) {
        write(byte.toInt())
    }

    fun write(string: String) {
        write(string.toByteArray())
    }

    fun write(bytes: ByteArray?) {
        if (bytes == null || bytes.isEmpty()) {
            return
        }
        if (limitMaxSize >= 0 && outputStream.size() >= limitMaxSize) {
            return
        }
        write(bytes, 0, bytes.size)
    }

    fun write(bytes: ByteArray, off: Int, len: Int) {
        if (limitMaxSize >= 0 && outputStream.size() >= limitMaxSize) {
            return
        } else {
            val size = if (limitMaxSize >= 0) {
                min(len, limitMaxSize - outputStream.size())
            } else {
                len
            }
            outputStream.write(bytes, off, size)
        }
    }

    /**写入00 00 数据*/
    fun writeSpace(length: Int) {
        val padArray = ByteArray(length)
        padArray.fill(0)
        write(padArray)
    }

    /**垫满长度
     * [length] 需要填满到指定的字节长度*/
    fun padLength(length: Int) {
        val size = outputStream.size()
        if (size >= length) {
            return
        }
        writeSpace(length - size)
    }
}

/**字节写入器*/
fun byteWriter(
    limitMaxSize: Int = -1,
    initialSize: Int = 32,
    action: ByteArrayWriter.() -> Unit
): ByteArray {
    return ByteArrayWriter(initialSize, limitMaxSize).run {
        action()
        toByteArray()
    }
}
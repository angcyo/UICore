package com.angcyo.http

import android.os.Build
import androidx.annotation.RequiresApi
import okio.*
import okio.ByteString.Companion.decodeHex
import java.io.*
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Okio操作类 https://github.com/square/okio
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/16 11:11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object RIo {
    private val PNG_HEADER = "89504e470d0a1a0a".decodeHex()

    @Throws(FileNotFoundException::class)
    fun copyFile(fromFilePath: String, toFilePath: String): Long {
        return copyFile(File(fromFilePath), toFilePath)
    }

    @Throws(FileNotFoundException::class)
    fun copyFile(fromFile: File, toFilePath: String): Long {
        return copyFile(FileInputStream(fromFile), toFilePath)
    }

    fun copyFile(from: InputStream, toFilePath: String): Long {
        return copyFile(from, File(toFilePath))
    }

    @Throws(FileNotFoundException::class)
    fun copyFile(fromFile: File, toFile: File): Long {
        return copyFile(FileInputStream(fromFile), toFile)
    }

    fun copyFile(from: InputStream, toFile: File): Long {
        var bufferedSource: BufferedSource? = null
        var bufferedSink: BufferedSink? = null
        try {
            val source = from.source()
            bufferedSource = source.buffer()
            if (!toFile.exists()) {
                toFile.createNewFile()
            }
            val sink = toFile.sink()
            bufferedSink = sink.buffer()
            val all = bufferedSource.readAll(bufferedSink)
            bufferedSink.flush()
            return all
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferedSink!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                from.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                bufferedSource!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return -1
    }

    /**
     * 追加数据到文件
     */
    fun appendToFile(filePath: String, data: String): Boolean {
        var bufferedSink: BufferedSink? = null
        try {
            val file = File(filePath)
            val sink = file.appendingSink()
            bufferedSink = sink.buffer()
            //bufferedSink.writeAll(Okio.source(file));
            bufferedSink.writeUtf8(data)
            bufferedSink.flush()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferedSink!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Throws(IOException::class)
    fun decodePng(`in`: InputStream) {
        `in`.source().buffer().use { pngSource ->
            val header = pngSource.readByteString(PNG_HEADER.size.toLong())
            if (header != PNG_HEADER) {
                throw IOException("Not a PNG.")
            }
            while (true) {
                val chunk = Buffer()

                // Each chunk is a length, type, data, and CRC offset.
                val length = pngSource.readInt()
                val type = pngSource.readUtf8(4)
                pngSource.readFully(chunk, length.toLong())
                val crc = pngSource.readInt()
                decodeChunk(type, chunk)
                if (type == "IEND") break
            }
        }
    }

    private fun decodeChunk(type: String, chunk: Buffer) {
        try {
            if (type == "IHDR") {
                val width = chunk.readInt()
                val height = chunk.readInt()
                System.out.printf("%08x: %s %d x %d%n", chunk.size, type, width, height)
            } else {
                System.out.printf("%08x: %s%n", chunk.size, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
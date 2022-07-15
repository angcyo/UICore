package com.angcyo.library.utils

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/15
 */
object ImageTypeUtil {

    enum class ImageType {
        JPEG, GIF, PNG, BMP, WEBP, UNKNOWN;

        companion object {
            fun of(type: String?): ImageType {
                if (TextUtils.isEmpty(type)) {
                    return UNKNOWN
                }
                if ("JPEG".equals(type, ignoreCase = true)) {
                    return JPEG
                }
                if ("GIF".equals(type, ignoreCase = true)) {
                    return GIF
                }
                if ("PNG".equals(type, ignoreCase = true)) {
                    return PNG
                }
                if ("BMP".equals(type, ignoreCase = true)) {
                    return BMP
                }
                return if ("WEBP".equals(type, ignoreCase = true)) {
                    WEBP
                } else UNKNOWN
            }
        }
    }

    fun getImageType(file: File?): String? {
        if (file == null || !file.isFile || !file.canRead()) return null
        var `is`: InputStream? = null
        return try {
            `is` = FileInputStream(file)
            getImageType(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                `is`!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getImageType(`is`: InputStream?): String? {
        return if (`is` == null) null else try {
            val bytes = ByteArray(8)
            if (`is`.read(bytes, 0, 8) != -1) getImageType(bytes) else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getImageType(bytes: ByteArray): String? {
        if (isJPEG(bytes)) return "JPEG"
        if (isGIF(bytes)) return "GIF"
        if (isPNG(bytes)) return "PNG"
        if (isBMP(bytes)) return "BMP"
        return if (isWebP(bytes)) "WEBP" else null
    }

    fun isJPEG(b: ByteArray): Boolean {
        return b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte()
    }

    fun isGIF(b: ByteArray): Boolean {
        return b.size >= 6 && b[0] == 'G'.toByte() &&
                b[1] == 'I'.toByte() && b[2] == 'F'.toByte() &&
                b[3] == '8'.toByte() && (b[4] == '7'.toByte() ||
                b[4] == '9'.toByte()) && b[5] == 'a'.toByte()
    }

    fun isPNG(b: ByteArray): Boolean {
        return (b.size >= 8 && b[0] == 137.toByte() &&
                b[1] == 80.toByte() && b[2] == 78.toByte() &&
                b[3] == 71.toByte() && b[4] == 13.toByte() &&
                b[5] == 10.toByte() && b[6] == 26.toByte() &&
                b[7] == 10.toByte())
    }

    fun isBMP(b: ByteArray): Boolean {
        return b.size >= 2 && b[0] == 0x42.toByte() && b[1] == 0x4d.toByte()
    }

    fun isWebP(b: ByteArray): Boolean {
        return b.size >= 4 && b[0] == 82.toByte() && b[1] == 73.toByte() && b[2] == 70.toByte() && b[3] == 70.toByte()
    }
}
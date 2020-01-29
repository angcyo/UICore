package com.angcyo.library.ex

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.text.format.Formatter
import com.angcyo.library.ex.FileEx.hexDigits
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and

/**
 * /kotlin/io/FileReadWrite.kt
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

object FileEx {
    val hexDigits =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
}

enum class SizeUnit {
    Byte, KB, MB, GB, TB, PB, Auto
}

/**文件是否存在*/
fun String.isFileExist(): Boolean {
    return try {
        val file = File(this)
        file.exists() && file.canRead()
    } catch (e: Exception) {
        false
    }
}


/**将文件内容, 转移到另一个文件*/
fun String.transferToFile(filePath: String) {
    if (this.equals(filePath, ignoreCase = true)) {
        return
    }
    var outputChannel: FileChannel? = null
    var inputChannel: FileChannel? = null
    try {
        inputChannel = FileInputStream(File(this)).channel
        outputChannel = FileOutputStream(File(filePath)).channel
        inputChannel.transferTo(0, inputChannel.size(), outputChannel)
        inputChannel.close()
    } finally {
        inputChannel?.close()
        outputChannel?.close()
    }
}

fun ByteArray.bytes2HexString(): String? {
    val len = this.size
    if (len <= 0) return null
    val ret = CharArray(len shl 1)
    var i = 0
    var j = 0
    while (i < len) {
        ret[j++] =
            hexDigits[((this[i].toInt() ushr 4) and 0x0f)]
        ret[j++] =
            hexDigits[(this[i] and 0x0f.toByte()).toInt()]
        i++
    }
    return String(ret)
}

/**获取文件md5值*/
fun File.md5(): String? {
    return getFileMD5()?.bytes2HexString()
}

fun String.file(): File {
    return File(this)
}

fun String?.fileSize(): Long {
    if (TextUtils.isEmpty(this)) {
        return 0L
    }
    val file = this!!.file()
    return if (file.exists()) {
        file.length()
    } else {
        0L
    }
}

/**格式化文件大小, 根据系统版本号选择实现方式*/
fun formatFileSize(context: Context, size: Long): String {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        size.fileSizeString()
    } else Formatter.formatFileSize(context, size)
}

fun String?.fileSizeString(): String {
    return fileSize().fileSizeString()
}

fun Long.fileSizeString(unit: SizeUnit = SizeUnit.Auto): String {
    val size = this
    var _unit = unit
    if (size < 0) {
        return ""
    }
    val KB = 1024.0
    val MB = KB * 1024
    val GB = MB * 1024
    val TB = GB * 1024
    val PB = TB * 1024
    if (_unit == SizeUnit.Auto) {
        _unit = if (size < KB) {
            SizeUnit.Byte
        } else if (size < MB) {
            SizeUnit.KB
        } else if (size < GB) {
            SizeUnit.MB
        } else if (size < TB) {
            SizeUnit.GB
        } else if (size < PB) {
            SizeUnit.TB
        } else {
            SizeUnit.PB
        }
    }
    return when (_unit) {
        SizeUnit.Byte -> size.toString() + "B"
        SizeUnit.KB -> String.format(Locale.US, "%.2fKB", size / KB)
        SizeUnit.MB -> String.format(Locale.US, "%.2fMB", size / MB)
        SizeUnit.GB -> String.format(Locale.US, "%.2fGB", size / GB)
        SizeUnit.TB -> String.format(Locale.US, "%.2fTB", size / TB)
        SizeUnit.PB -> String.format(Locale.US, "%.2fPB", size / PB)
        else -> size.toString() + "B"
    }
}

/**
 * 获取文件的MD5校验码
 *
 * @param file 文件
 * @return 文件的MD5校验码
 */
fun File.getFileMD5(): ByteArray? {
    var dis: DigestInputStream? = null
    try {
        val fis = FileInputStream(this)
        var md = MessageDigest.getInstance("MD5")
        dis = DigestInputStream(fis, md)
        val buffer = ByteArray(1024 * 256)
        while (dis.read(buffer) > 0);
        md = dis.messageDigest
        return md.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        dis?.close()
    }
    return null
}
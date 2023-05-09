package com.angcyo.library.utils

import android.content.Context
import android.net.Uri
import com.angcyo.library.component.lastContext
import java.io.File
import java.io.InputStream

/**
 * 根据文件头, 判断文件类型
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/21
 */
object FileType {

    /**从流中, 读取头几个字节, 获取文件类型的后缀名*/
    fun getFileType(inputStream: InputStream?): String? {
        val b = ByteArray(8)
        try {
            inputStream?.read(b, 0, 8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getFileType(b)
    }

    /**获取文件类型对应的后缀名*/
    fun getFileType(bytes: ByteArray): String? {
        val fileCode = bytes2HexString(bytes)
        return when {
            fileCode.startsWith("FFD8FF") -> ".jpg"
            fileCode.startsWith("89504E47") -> ".png"
            fileCode.startsWith("47494638") -> ".gif"
            fileCode.startsWith("49492A00") -> ".tif"
            fileCode.startsWith("424D") -> ".bmp"
            fileCode.startsWith("41433130") -> ".dwg"
            fileCode.startsWith("38425053") -> ".psd"
            fileCode.startsWith("7B5C727466") -> ".rtf"
            fileCode.startsWith("3C3F786D6C") -> ".xml"
            fileCode.startsWith("68746D6C3E") -> ".html"
            //fileCode.startsWith("44656C69766572792D646174653A") -> ".eml"
            //fileCode.startsWith("CFAD12FEC5FD746F") -> ".dbx"
            fileCode.startsWith("2142444E") -> ".pst"
            fileCode.startsWith("D0CF11E0") -> ".xls/doc"
            //fileCode.startsWith("5374616E64617264204A") -> ".mdb"
            fileCode.startsWith("FF575043") -> ".wpd"
            //fileCode.startsWith("252150532D41646F6265") -> ".eps/ps"
            fileCode.startsWith("255044462D312E") -> ".pdf"
            fileCode.startsWith("AC9EBD8F") -> ".qdf"
            fileCode.startsWith("E3828596") -> ".pwl"
            fileCode.startsWith("504B0304") -> ".zip"
            fileCode.startsWith("52617221") -> ".rar"
            fileCode.startsWith("57415645") -> ".wav"
            fileCode.startsWith("41564920") -> ".avi"
            fileCode.startsWith("2E7261FD") -> ".ram"
            fileCode.startsWith("2E524D46") -> ".rm"
            fileCode.startsWith("000001BA") -> ".mpg"
            fileCode.startsWith("000001B3") -> ".mpg"
            fileCode.startsWith("6D6F6F76") -> ".mov"
            fileCode.startsWith("3026B2758E66CF11") -> ".asf"
            fileCode.startsWith("4D546864") -> ".mid"
            else -> null
        }
    }

    fun bytes2HexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(b.toInt() and 0xFF)
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex.uppercase())
        }
        return sb.toString()
    }
}

/**@return 返回文件类型的后缀名*/
fun Uri.fileType(context: Context = lastContext): String? {
    return try {
        context.contentResolver.openInputStream(this).use { fileType() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**@return 返回文件类型的后缀名*/
fun File.fileType(): String? {
    return try {
        inputStream().use { FileType.getFileType(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**@return 返回文件类型的后缀名*/
fun InputStream?.fileType(): String? {
    return try {
        FileType.getFileType(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
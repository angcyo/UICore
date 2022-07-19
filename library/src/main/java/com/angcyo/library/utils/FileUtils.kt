package com.angcyo.library.utils

import android.content.Context
import android.graphics.Bitmap
import com.angcyo.library.*
import com.angcyo.library.ex.file
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android Q 扩展的程序目录操作
 *
 * 卸载后会丢失, 不需要申请权限
 *
 * /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$type
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 * 写入文件的数据, 支持以下数据类型
 * [String]
 * [ByteArray]
 * [Bitmap]
 * */
typealias FileTextData = Any

object FileUtils {

    /**允许写入单个文件的最大大小10mb, 之后会重写*/
    var fileMaxSize: Long = 10 * 1024 * 1024

    /**所有文件写入的在此根目录下*/
    var rootFolder: String = getAppString("schema") ?: "angcyo"

    /**获取文件夹路径*/
    var onGetFolderPath: (folderName: String) -> String = {
        "$rootFolder${File.separator}$it"
    }

    /**[dataDir]:[/data/user/0/com.wayto.plugin.gb.security]
     * *//*
    fun appRootFolder(context: Context = app()): File? {
        return context.externalMediaDirs?.firstOrNull()
    }*/


    /**扩展目录下的指定文件夹
     * [/storage/emulated/0/Android/data/包名/files/${schema}/${folder}]*/
    fun appRootExternalFolder(folder: String = "", context: Context = app()): File {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$folder

        /*
        ///data/user/0/com.angcyo.uicore.demo/cache
        context.cacheDir

        ///storage/emulated/0/Android/data/com.angcyo.opencv.demo/cache
        context.externalCacheDir

        ///data/user/0/com.angcyo.uicore.demo/files
        context.filesDir

        ///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files
        context.getExternalFilesDir("")*/

        //context.getExternalFilesDir(onGetFolderPath(folder))
        return libFolderPath(onGetFolderPath(folder), context).file()
    }

    /**
     * [folder] 文件夹名字
     * [name] 文件夹下面的文件名
     *
     * 返回对应的文件, 可以直接进行读写, 不需要权限请求
     * */
    fun appRootExternalFolderFile(folder: String, name: String): File {
        val externalFilesDir = appRootExternalFolder(folder)
        return File(externalFilesDir, name)
    }

    /** Android Q 写入扩展的程序目录下的文件数据 */
    fun writeExternal(
        folder: String,
        name: String,
        data: FileTextData,
        append: Boolean = true /*false 强制重新写入*/
    ): String? {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$type
        var filePath: String? = null

        try {
            appRootExternalFolderFile(folder, name).apply {
                filePath = writeExternal(this, data, append)
            }
        } catch (e: Exception) {
            L.e("写入文件失败:$e")
        }

        return filePath
    }

    /**[append]=true 根据文件大小智能判断是否要重写
     * @return 文件路径*/
    fun writeExternal(file: File, data: FileTextData, append: Boolean = true): String? {
        var filePath: String? = null

        try {
            file.parentFile?.mkdirs()
            file.apply {
                filePath = absolutePath

                if (data is Bitmap) {
                    //保存图片
                    outputStream().use {
                        data.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                } else {
                    when {
                        length() >= fileMaxSize || !append -> if (data is ByteArray) {
                            writeBytes(data)
                        } else {
                            writeText(data.toString())
                        }
                        else -> if (data is ByteArray) {
                            appendBytes(data)
                        } else {
                            appendText(data.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            L.e("写入文件失败:$e")
        }

        return filePath
    }

    /**从APP扩展目录下读取文件数据*/
    fun readExternal(folder: String, name: String): String? {
        try {
            return appRootExternalFolderFile(folder, name).readText()
        } catch (e: Exception) {
            L.e("读取文件失败:$e")
        }
        return null
    }
}

/**[UUID]*/
fun uuid() = UUID.randomUUID().toString()

/**随机一个文件名*/
fun fileNameUUID(suffix: String = ""): String {
    if (suffix.isEmpty()) {
        return uuid()
    }
    return if (suffix.startsWith(".")) {
        "${uuid()}$suffix"
    } else {
        "${uuid()}.$suffix"
    }
}

/**获取一个时间文件名*/
fun fileName(pattern: String = "yyyy-MM-dd_HH-mm-ss-SSS", suffix: String = ""): String {
    val dateFormat: DateFormat = SimpleDateFormat(pattern, Locale.CHINA)
    return dateFormat.format(Date()) + suffix
}

/**获取一个文件路径*/
fun filePath(folderName: String, fileName: String = fileNameUUID()): String {
    return "${FileUtils.appRootExternalFolder(folder = folderName).absolutePath}${File.separator}${fileName}"
}

/**获取一个文件夹路径*/
fun folderPath(folderName: String): String {
    return FileUtils.appRootExternalFolder(folder = folderName).absolutePath
        ?: app().cacheDir.absolutePath
}

fun logFileName() = fileName("yyyy-MM-dd", ".log")

/**[append]=true 根据文件大小智能判断是否要重写*/
fun File.writeText(data: FileTextData?, append: Boolean) {
    FileUtils.writeExternal(this, data ?: "null", append)
}

fun String?.writeTo(file: File, append: Boolean = true) =
    FileUtils.writeExternal(file, this ?: "null", append)

fun String?.writeTo(filePath: String?, append: Boolean = true): String? {
    val file = filePath?.file()
    if (file != null) {
        return FileUtils.writeExternal(file, this ?: "null", append)
    }
    return null
}

/**获取文件夹全路径*/
fun String.logFilePath(name: String = logFileName()): String {
    return FileUtils.appRootExternalFolderFile(this, name).absolutePath
}

/**
 * 带Scheme的文件夹路径
 * [com.angcyo.library.LibraryKt.libFolderPath]
 * */
fun appFolderPath(folder: String = "", context: Context = app()): String {
    return FileUtils.appRootExternalFolder(folder, context).absolutePath
}
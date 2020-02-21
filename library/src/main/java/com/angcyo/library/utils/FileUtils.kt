package com.angcyo.library.utils

import android.content.Context
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.getAppString
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
object FileUtils {

    /**所有文件写入的在此根目录下*/
    var rootFolder: String = getAppString("schema") ?: ""

    /**获取文件夹路径*/
    var onGetFolderPath: (folderName: String) -> String = {
        "$rootFolder/$it"
    }

    /**APP扩展根目录*/
    fun appRootExternalFolder(context: Context = app()): File? {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files
        return context.getExternalFilesDir("")
    }

    /**扩展目录下的指定文件夹*/
    fun appRootExternalFolder(context: Context = app(), folder: String): File? {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$folder
        return context.getExternalFilesDir(onGetFolderPath(folder))
    }

    /**
     * [folder] 文件夹名字
     * [name] 文件夹下面的文件名
     *
     * 返回对应的文件, 可以直接进行读写, 不需要权限请求
     * */
    fun appRootExternalFolderFile(
        context: Context? = app(),
        folder: String,
        name: String
    ): File? {
        if (context == null) {
            return null
        }
        val externalFilesDir = appRootExternalFolder(context, folder)
        var file: File? = null
        externalFilesDir?.also {
            file = File(it, name)
        }
        return file
    }

    /** Android Q 写入扩展的程序目录下的文件数据 */
    fun writeExternal(
        context: Context? = app(),
        folder: String,
        name: String,
        data: String,
        append: Boolean = true
    ): String? {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$type
        var filePath: String? = null

        try {
            appRootExternalFolderFile(context, folder, name)?.apply {
                filePath = absolutePath
                if (append) {
                    appendText(data)
                } else {
                    writeText(data)
                }
            }
        } catch (e: Exception) {
            L.e("写入文件失败:$e")
        }

        return filePath
    }

    /**从APP扩展目录下读取文件数据*/
    fun readExternal(
        context: Context = app(),
        folder: String,
        name: String
    ): String? {
        try {
            return appRootExternalFolderFile(context, folder, name)?.readText()
        } catch (e: Exception) {
            L.e("读取文件失败:$e")
        }
        return null
    }
}

/**随机一个文件名*/
fun fileNameUUID(suffix: String = ""): String {
    return UUID.randomUUID().toString() + suffix
}

/**获取一个时间文件名*/
fun fileName(pattern: String = "yyyy-MM-dd_HH-mm-ss-SSS", suffix: String = ""): String {
    val dateFormat: DateFormat = SimpleDateFormat(pattern, Locale.CHINA)
    return dateFormat.format(Date()) + suffix
}

/**获取一个文件路径*/
fun filePath(folderName: String, fileName: String): String {
    return "${FileUtils.appRootExternalFolder(folder = folderName)?.absolutePath}${File.separator}${fileName}"
}

/**获取一个文件夹路径*/
fun folderPath(folderName: String): String {
    return FileUtils.appRootExternalFolder(folder = folderName)?.absolutePath
        ?: app().cacheDir.absolutePath
}
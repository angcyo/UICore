package com.angcyo.library.utils

import android.content.Context
import com.angcyo.library.L
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

    /**APP扩展根目录*/
    fun appRootExternalFolder(context: Context): File? {
        return context.getExternalFilesDir("")
    }

    fun appExternalFolder(
        context: Context?,
        folder: String,
        name: String
    ): File? {
        if (context == null) {
            return null
        }
        val externalFilesDir = context.getExternalFilesDir(folder)
        var file: File? = null
        externalFilesDir?.also {
            file = File(it, name)
        }
        return file
    }

    /** Android Q 写入扩展的程序目录下的文件数据 */
    fun writeExternal(
        context: Context?,
        folder: String,
        name: String,
        data: String,
        append: Boolean = true
    ): String? {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$type
        var filePath: String? = null

        try {
            appExternalFolder(context, folder, name)?.apply {
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
        context: Context,
        folder: String,
        name: String
    ): String? {
        try {
            return appExternalFolder(context, folder, name)?.readText()
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

fun fileName(pattern: String = "yyyy-MM-dd_HH-mm-ss-SSS", suffix: String = ""): String {
    val dateFormat: DateFormat = SimpleDateFormat(pattern, Locale.CHINA)
    return dateFormat.format(Date()) + suffix
}
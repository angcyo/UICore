package com.angcyo.core.component.file

import android.content.Context
import com.angcyo.coroutine.CoroutineErrorHandler
import com.angcyo.coroutine.launchGlobal
import com.angcyo.library.app
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.fileName
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

/**
 * APP扩展数据扩展目录下的 文件操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object DslFileHelper {
    var appContext: Context? = null
        get() = field ?: app()

    /**异步文件写入*/
    var async = true

    val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.CHINA)

    /**返回文件路径*/
    fun write(
        folder: String,
        name: String = fileName("yyyy-MM-dd"),
        data: String
    ): String? {
        if (async) {
            launchGlobal(Dispatchers.IO + CoroutineErrorHandler()) {
                FileUtils.writeExternal(appContext, folder, name, data)
            }
        } else {
            FileUtils.writeExternal(appContext, folder, name, data)
        }
        //返回文件路径
        return FileUtils.appRootExternalFolderFile(appContext, folder, name)?.absolutePath
    }

    fun log(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.LOG_FOLDER_NAME, name, _wrapData(data))

    fun http(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.HTTP_FOLDER_NAME, name, _wrapData(data))

    fun ui(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.UI_FOLDER_NAME, name, _wrapData(data))

    fun crash(name: String = fileName(), data: String) =
        write(com.angcyo.library.utils.Constant.CRASH_FOLDER_NAME, name, _wrapData(data))

    fun down(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.DOWN_FOLDER_NAME, name, _wrapData(data))

    fun camera(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.CAMERA_FOLDER_NAME, name, _wrapData(data))

    fun other(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.OTHER_FOLDER_NAME, name, _wrapData(data))


    fun error(name: String = fileName("yyyy-MM-dd"), data: String) =
        write(com.angcyo.library.utils.Constant.ERROR_FOLDER_NAME, name, _wrapData(data))

    fun _wrapData(data: String): String {
        return buildString {
            appendln(dateFormat.format(Date()))
            append(data)
        }
    }
}

fun String?.writeTo(
    folder: String = Constant.LOG_FOLDER_NAME,
    name: String = fileName("yyyy-MM-dd")
) {
    DslFileHelper.write(folder, name, this ?: "null")
}
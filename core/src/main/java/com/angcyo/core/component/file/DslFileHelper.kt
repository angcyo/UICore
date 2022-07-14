package com.angcyo.core.component.file

import android.content.Context
import com.angcyo.coroutine.CoroutineErrorHandler
import com.angcyo.coroutine.launchGlobal
import com.angcyo.library.app
import com.angcyo.library.ex.wrapLog
import com.angcyo.library.utils.*
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

    /**返回文件路径
     *
     * adb pull /sdcard/android/data/xxx/files/xxx/ ./
     * code .
     * */
    fun write(
        folder: String /*文件夹名, 相对于应用目录下的/files/文件夹*/,
        name: String = logFileName() /*文件名, 默认当天日期*/,
        data: FileTextData /*需要写入的数据*/,
        append: Boolean = true
    ): String? {
        return if (async) {
            launchGlobal(Dispatchers.IO + CoroutineErrorHandler()) {
                FileUtils.writeExternal(folder, name, data, append)
            }
            //返回文件路径
            FileUtils.appRootExternalFolderFile(folder, name).absolutePath
        } else {
            FileUtils.writeExternal(folder, name, data, append)
        }
    }

    fun log(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.LOG_FOLDER_NAME, name, _wrapData(data), append)

    fun http(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.HTTP_FOLDER_NAME, name, _wrapData(data), append)

    fun ui(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.UI_FOLDER_NAME, name, _wrapData(data), append)

    fun crash(name: String = fileName(suffix = ".log"), data: String, append: Boolean = true) =
        write(Constant.CRASH_FOLDER_NAME, name, _wrapData(data), append)

    fun down(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.DOWN_FOLDER_NAME, name, _wrapData(data), append)

    fun camera(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.CAMERA_FOLDER_NAME, name, _wrapData(data), append)

    fun other(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.OTHER_FOLDER_NAME, name, _wrapData(data), append)

    fun error(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.ERROR_FOLDER_NAME, name, _wrapData(data), append)

    fun push(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.PUSH_FOLDER_NAME, name, _wrapData(data), append)

    fun _wrapData(data: String): String {
        return buildString {
            appendln()
            append(dateFormat.format(Date()))
            appendln()
            append(data)
        }
    }

    fun _wrapData2(data: CharSequence): String {
        return buildString {
            append(dateFormat.format(Date()))
            appendln()
            append(data)
            appendln()
        }
    }
}

/**获取一个文件路径, 带Scheme的路径
 * [com.angcyo.library.utils.FileUtils.getRootFolder]
 * [com.angcyo.library.LibraryKt.libFilePath]*/
fun appFilePath(name: String, folder: String = ""): String {
    return FileUtils.appRootExternalFolderFile(folder, name).absolutePath
}

fun CharSequence.wrapData() = DslFileHelper._wrapData2(this)

/**写入数据到文件
 * [folder] 文件夹的名字, 会自动追加Scheme
 * [name] 文件的名字*/
fun FileTextData?.writeTo(
    folder: String = Constant.LOG_FOLDER_NAME,
    name: String = logFileName(),
    append: Boolean = true
): String? {
    if (this == null) {
        return FileUtils.appRootExternalFolderFile(folder, name).absolutePath
    }
    return DslFileHelper.write(folder, name, this, append)
}

/**将日志写入到指定的日志文件[log.log]*/
fun String.writeToLog(name: String = "log.log"): String {
    wrapLog().writeTo(Constant.LOG_FOLDER_NAME, name)
    return this
}

/**将日志写入到[error.log]*/
fun String.writeErrorLog(): String {
    writeToLog("error.log")
    return this
}
package com.angcyo.core.component.file

import android.content.Context
import com.angcyo.coroutine.CoroutineErrorHandler
import com.angcyo.coroutine.launchGlobal
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.wrapLog
import com.angcyo.library.isMain
import com.angcyo.library.libCacheFile
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
        folder: String, /*文件夹名, 相对于应用目录下的/files/文件夹*/
        name: String = logFileName(), /*文件名, 默认当天日期*/
        data: FileTextData, /*需要写入的数据*/
        append: Boolean = true,
        recycle: Boolean = false, /*图片数据保存时, 是否要回收图片*/
    ): String? {
        return if (async && isMain() /*只在主线程中才使用异步操作*/) {
            launchGlobal(Dispatchers.IO + CoroutineErrorHandler()) {
                FileUtils.writeExternal(folder, name, data, append, recycle)
            }
            //返回文件路径
            FileUtils.appRootExternalFolderFile(folder, name).absolutePath
        } else {
            FileUtils.writeExternal(folder, name, data, append, recycle)
        }
    }

    fun log(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.LOG_FOLDER_NAME, name, _wrapData(data), append)

    fun http(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.HTTP_FOLDER_NAME, name, _wrapData(data), append)

    fun ui(name: String = logFileName(), data: String, append: Boolean = true) =
        write(Constant.UI_FOLDER_NAME, name, _wrapData(data), append)

    fun crash(name: String = fileNameTime(suffix = ".log"), data: String, append: Boolean = true) =
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
 * [com.angcyo.library.LibraryKt.libFilePath]
 * [com.angcyo.library.utils.FileUtilsKt.appFolderPath]
 * */
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
    append: Boolean = true,
    recycle: Boolean = false,
): String? {
    if (this == null) {
        return FileUtils.appRootExternalFolderFile(folder, name).absolutePath
    }
    return DslFileHelper.write(folder, name, this, append, recycle)
}

/**写入到缓存目录*/
fun FileTextData?.writeToCache(
    folder: String = Constant.LOG_FOLDER_NAME,
    name: String = logFileName(),
    append: Boolean = true,
    recycle: Boolean = false,
): String? {
    val libCacheFile = libCacheFile(name, folder)
    if (this == null) {
        return libCacheFile.absolutePath
    }
    return FileUtils.writeExternal(libCacheFile, this, append, recycle)
}

/**将日志写入到指定的日志文件[log.log], 默认在[log]文件夹下
 * [logLevel] 同时输出到控制台的日志级别
 * [log/log.log]
 * [com.angcyo.library.utils.Constant.LOG_FOLDER_NAME]*/
fun String.writeToLog(name: String = LogFile.log, logLevel: Int = L.NONE): String {
    wrapLog().writeTo(Constant.LOG_FOLDER_NAME, name)
    L.log(logLevel, this)
    return this
}

/**将日志写入到[error.log]
 * [logLevel] 同时输出到控制台的日志级别
 * [log/error.log]
 * */
fun String.writeErrorLog(logLevel: Int = L.NONE): String {
    writeToLog(LogFile.error, logLevel)
    return this
}

/**将日志写入到[http.log]
 * [logLevel] 同时输出到控制台的日志级别
 * [log/http.log]
 * */
fun String.writeHttpLog(logLevel: Int = L.NONE): String {
    writeToLog(LogFile.http, logLevel)
    return this
}

/**将日志写入到[perf.log]
 * [logLevel] 同时输出到控制台的日志级别
 * [log/perf.log]
 * */
fun String.writePerfLog(logLevel: Int = L.INFO): String {
    writeToLog(LogFile.perf, logLevel)
    return this
}
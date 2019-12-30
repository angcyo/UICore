package com.angcyo.core.component.file

import android.content.Context
import com.angcyo.coroutine.CoroutineErrorHandler
import com.angcyo.coroutine.launch
import com.angcyo.library.getAppString
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.fileName
import kotlinx.coroutines.Dispatchers
import java.text.DateFormat
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

    /**所有文件写入的在此根目录下*/
    var rootFolder: String = getAppString("schema") ?: ""

    /**常用日志文件夹*/
    var log = "log"
    var http = "http"
    var ui = "ui"
    var crash = "crash"
    var down = "down"
    var camera = "camera"
    var other = "other"
    var error = "error"

    /**异步文件写入*/
    var async = true

    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.CHINA)

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**返回文件路径*/
    fun write(
        folder: String,
        name: String,
        data: String
    ): String? {
        val f = "$rootFolder/$folder"
        if (async) {
            launch(Dispatchers.IO + CoroutineErrorHandler()) {
                FileUtils.writeExternal(appContext, f, name, data)
            }
        } else {
            FileUtils.writeExternal(appContext, f, name, data)
        }
        return FileUtils.appExternalFolder(appContext, folder, name)?.absolutePath
    }

    fun log(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(log, name, _wrapData(data))
    }

    fun http(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(http, name, _wrapData(data))
    }

    fun ui(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(ui, name, _wrapData(data))
    }

    fun crash(name: String = fileName(), data: String) {
        write(crash, name, _wrapData(data))
    }

    fun down(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(down, name, _wrapData(data))
    }

    fun camera(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(camera, name, _wrapData(data))
    }

    fun other(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(other, name, _wrapData(data))
    }

    fun error(name: String = fileName("yyyy-MM-dd"), data: String) {
        write(error, name, _wrapData(data))
    }

    fun _wrapData(data: String): String {
        return buildString {
            appendln(dateFormat.format(Date()))
            append(data)
        }
    }

    fun test() {
        //write("f", "f.log", "test")
        //rootFolder = "demo"
        //write("f2", "f2.log", "test2")
        1 / 0
    }
}
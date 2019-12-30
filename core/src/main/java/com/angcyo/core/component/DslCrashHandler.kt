package com.angcyo.core.component

import android.content.Context
import android.content.Intent
import com.angcyo.DslAHelper
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.utils.Device
import com.angcyo.library.L
import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.StringWriter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class DslCrashHandler : Thread.UncaughtExceptionHandler {
    companion object {
        fun init(context: Context) {
            DslCrashHandler().install(context)
        }
    }

    /**崩溃之后, 写入文件头部的信息*/
    var crashHeadMsg: String? = null

    /**崩溃之后, 需要启动的Intent*/
    var crashLaunchIntent: Intent? = null

    var _applicationContext: Context? = null
    var _defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun install(context: Context) {
        _applicationContext = context.applicationContext
        _defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)

        if (crashLaunchIntent == null) {
            crashLaunchIntent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        L.w("全局异常#${t.name}:$e")
        e.printStackTrace()

        DslFileHelper.async = false
        DslFileHelper.crash(data = buildString {

            crashHeadMsg?.let { appendln(it) }

            Device.buildString(this)
            appendln()

            _applicationContext?.let {
                Device.screenInfo(it, this)
                appendln()
                Device.deviceInfo(it, this)
            }

            appendln()
            append(_getThrowableInfo(e))

        })

        DslFileHelper.async = true

        _defaultUncaughtExceptionHandler?.uncaughtException(t, e)

        _applicationContext?.let {
            DslAHelper(it).apply {
                crashLaunchIntent?.let { start(it) }
                //doIt()
            }
        }
    }

    fun _getThrowableInfo(e: Throwable): String {
        val stringWriter = StringWriter()
        val pw = PrintWriter(BufferedWriter(stringWriter))
        e.printStackTrace(pw)
        pw.close()
        return stringWriter.toString()
    }
}
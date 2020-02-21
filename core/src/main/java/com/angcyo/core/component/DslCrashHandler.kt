package com.angcyo.core.component

import android.content.Context
import android.content.Intent
import com.angcyo.DslAHelper
import com.angcyo.DslFHelper
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.library.L
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.isRelease
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.utils.Device
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
        const val KEY_IS_CRASH = "is_crash"
        const val KEY_CRASH_FILE = "crash_file"
        const val KEY_CRASH_MESSAGE = "crash_message"
        const val KEY_CRASH_TIME = "crash_time"

        fun init(context: Context) {
            DslCrashHandler().install(context)
        }

        /** 检查最后一次是否有异常未处理 */
        fun checkCrash(
            clear: Boolean = false,
            crashAction: (filePath: String?, message: String?, crashTime: String?) -> Unit = { _, _, _ -> }
        ): Boolean {
            val isCrash = KEY_IS_CRASH.hawkGet()

            if (clear) {
                KEY_IS_CRASH.hawkPut(null)
            }

            return if (isCrash.isNullOrBlank()) {
                false
            } else {
                crashAction(
                    KEY_CRASH_FILE.hawkGet(),
                    KEY_CRASH_MESSAGE.hawkGet(),
                    KEY_CRASH_TIME.hawkGet()
                )
                true
            }
        }
    }

    /**崩溃之后, 写入文件头部的信息*/
    var crashHeadMsg: String? = null

    /**崩溃之后, 需要启动的Intent, 默认是程序启动界面*/
    var crashLaunchIntent: Intent? = null

    /**崩溃之后, 是否启动[crashLaunchIntent]*/
    var crashLaunch = isRelease()

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

        //异常退出
        KEY_IS_CRASH.hawkPut("true")
        //异常时间
        KEY_CRASH_TIME.hawkPut(nowTimeString())

        DslFileHelper.async = false
        DslFileHelper.crash(data = buildString {

            //自定义的头部信息
            crashHeadMsg?.let { appendln(it) }

            //apk编译信息
            Device.buildString(this)
            appendln()

            //fm日志信息
            appendln(DslFHelper.fragmentManagerLog)
            appendln()

            //屏幕信息, 设备信息
            _applicationContext?.let {
                Device.screenInfo(it, this)
                appendln()
                Device.deviceInfo(it, this)
            }

            //异常错误信息
            appendln()
            append(_getThrowableInfo(e))

            //异常概述
            KEY_CRASH_MESSAGE.hawkPut(e.message)

        })?.apply {
            //异常文件
            KEY_CRASH_FILE.hawkPut(this)
        }

        DslFileHelper.async = true

        _defaultUncaughtExceptionHandler?.uncaughtException(t, e)

        if (crashLaunch) {
            _applicationContext?.let {
                DslAHelper(it).apply {
                    crashLaunchIntent?.let { start(it) }
                    doIt()
                }
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
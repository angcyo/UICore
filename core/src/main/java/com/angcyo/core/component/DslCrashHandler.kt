package com.angcyo.core.component

import android.content.Context
import android.content.Intent
import com.angcyo.DslAHelper
import com.angcyo.DslFHelper
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.writeErrorLog
import com.angcyo.library.component.work.Trackers
import com.angcyo.library.ex.currentApplication
import com.angcyo.library.ex.file
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isRelease
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.ex.shareFile
import com.angcyo.library.ex.toStr
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.fileNameTime
import java.io.BufferedWriter
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeoutException

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class DslCrashHandler : Thread.UncaughtExceptionHandler {

    companion object {

        /**keys*/
        const val KEY_IS_CRASH = "is_crash"
        const val KEY_CRASH_FILE = "crash_file"
        const val KEY_CRASH_MESSAGE = "crash_message"
        const val KEY_CRASH_TIME = "crash_time"

        /**是否自动获取启动的[Intent]*/
        var OBTAIN_LAUNCH_INTENT: Boolean = isDebug()

        /**初始化*/
        fun init(context: Context, obtainLaunchIntent: Boolean = OBTAIN_LAUNCH_INTENT) {
            DslCrashHandler().install(context, obtainLaunchIntent)
        }

        /**清空崩溃标识*/
        fun clear() {
            KEY_IS_CRASH.hawkPut(null)
        }

        /** 检查最后一次是否有异常未处理 */
        fun checkCrash(
            clear: Boolean = false,
            crashAction: (filePath: String?, message: String?, crashTime: String?) -> Unit = { _, _, _ -> }
        ): Boolean {
            val isCrash: String? = KEY_IS_CRASH.hawkGet()

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

        /**判断是否有崩溃*/
        fun haveCrash(): Boolean = !KEY_IS_CRASH.hawkGet().isNullOrBlank()

        /**崩溃的文件名*/
        fun currentCrashName() = fileNameTime("yyyy-MM-dd", ".log")

        /**指定某一天的崩溃日志文件*/
        fun currentCrashFile(fileName: String? = null): File {
            val name = fileName ?: currentCrashName()
            val folder = currentApplication()?.getExternalFilesDir("crash")
            val file = File(folder, name)
            return file
        }

        /**分享日志文件
         * [fileName] 指定要分享的文件名, */
        fun shareCrashLog(fileName: String? = null) {
            if (fileName == null) {
                //未指定文件名, 则获取最后一次崩溃的文件
                val file = KEY_CRASH_FILE.hawkGet()?.file() ?: currentCrashFile()
                file.shareFile()
            } else {
                currentCrashFile(fileName).shareFile()
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

    fun install(context: Context, obtainLaunchIntent: Boolean) {
        _applicationContext = context.applicationContext
        _defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)

        if (crashLaunchIntent == null && obtainLaunchIntent) {
            crashLaunchIntent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        "全局异常#${t.name}:${e.toStr()}".writeErrorLog()
        e.printStackTrace()

        //异常退出
        KEY_IS_CRASH.hawkPut("true")
        //异常时间
        KEY_CRASH_TIME.hawkPut(nowTimeString())

        val oldAsync = DslFileHelper.async
        DslFileHelper.async = false
        DslFileHelper.crash(currentCrashName(), buildString {

            //自定义的头部信息
            crashHeadMsg?.let { appendLine(it) }

            //apk编译信息
            Device.buildString(this)
            appendLine()

            //fm日志信息
            appendLine(DslFHelper.fragmentManagerLog)
            appendLine()

            //屏幕信息, 设备信息
            _applicationContext?.let {
                Device.screenInfo(it, this)
                appendLine()
                Device.deviceInfo(it, this)
            }

            appendLine()
            append(Trackers.getInstance().networkStateTracker.activeNetworkState.toString())

            //异常错误信息
            appendLine()
            append(_getThrowableInfo(e))

            //异常概述
            KEY_CRASH_MESSAGE.hawkPut(e.message)

        })?.apply {
            //异常文件
            KEY_CRASH_FILE.hawkPut(this)
        }

        DslFileHelper.async = oldAsync

        if (t.name == "FinalizerWatchdogDaemon" && e is TimeoutException) {
            KEY_IS_CRASH.hawkPut("false")
        } else {
            _defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }

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
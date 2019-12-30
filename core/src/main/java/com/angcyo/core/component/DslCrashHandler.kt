package com.angcyo.core.component

import android.content.Context
import com.angcyo.library.L

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

    var _applicationContext: Context? = null
    var _defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun install(context: Context) {
        _applicationContext = context.applicationContext
        _defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        L.w("全局异常#${t.name}:$e")
        e.printStackTrace()

        _defaultUncaughtExceptionHandler?.uncaughtException(t, e)
    }
}
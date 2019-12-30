package com.angcyo.core

import android.app.Application
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.library.L
import com.angcyo.library.Library
import com.angcyo.library.ex.isDebug
import com.angcyo.library.getAppString

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class CoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //必须第一个初始化
        Library.init(this, isDebug())
        DslFileHelper.init(this)
        DslCrashHandler.init(this)
        L.init(getAppString("app_name") ?: "Log", isDebug())
    }
}
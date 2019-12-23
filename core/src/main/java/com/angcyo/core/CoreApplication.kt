package com.angcyo.core

import android.app.Application
import com.angcyo.library.L
import com.angcyo.library.Library
import com.angcyo.library.ex.isDebug

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
        Library.init(this, isDebug())

        val resources = resources
        val appNameId = resources.getIdentifier("app_name", "string", packageName)
        L.init(
            if (appNameId > 0) {
                resources.getString(appNameId)
            } else "Log", isDebug()
        )
    }
}
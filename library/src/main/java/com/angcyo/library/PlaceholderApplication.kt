package com.angcyo.library

import android.app.Application
import android.content.res.Resources

/**
 * 占位的空实现
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class PlaceholderApplication : Application() {

    override fun getPackageName(): String = BuildConfig.LIBRARY_PACKAGE_NAME

    override fun getResources(): Resources {
        return Resources.getSystem()
    }

}
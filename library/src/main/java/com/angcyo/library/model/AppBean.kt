package com.angcyo.library.model

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
data class AppBean(
    var packageName: String,
    var versionName: String?,
    var versionCode: Long,
    var appIcon: Drawable?,
    var appName: CharSequence?,
    var packageInfo: PackageInfo?,
    var des: String? = null
)
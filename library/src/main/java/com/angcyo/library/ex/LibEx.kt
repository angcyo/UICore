package com.angcyo.library.ex

import com.angcyo.library.BuildConfig

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun isRelease(): Boolean = "release".equals(BuildConfig.BUILD_TYPE, true)

fun isDebugType() = "debug".equals(BuildConfig.BUILD_TYPE, true)

fun isDebug() = BuildConfig.DEBUG

fun Any?.hash(): String? {
    return this?.hashCode()?.run { Integer.toHexString(this) }
}

fun Any.simpleHash(): String {
    return "${this.javaClass.simpleName}(${this.hash()})"
}

fun Any.simpleClassName(): String {
    return this.javaClass.simpleName
}

fun Any.className(): String {
    return this.javaClass.name
}

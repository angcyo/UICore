package com.angcyo.library.ex

import com.angcyo.library.BuildConfig

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
fun isRelease(): Boolean {
    return BuildConfig.BUILD_TYPE == "release"
}

fun isDebug() = BuildConfig.DEBUG

fun Any?.hash(): String? {
    return this?.hashCode()?.run { Integer.toHexString(this) }
}

public fun Any.simpleHash(): String {
    return "${this.javaClass.simpleName}(${this.hash()})"
}

public fun Any.simpleName(): String {
    return this.javaClass.simpleName
}

public fun Any.name(): String {
    return this.javaClass.name
}
package com.angcyo.core

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="Application级别的单例模式">

fun Activity.core(action: CoreApplication.() -> Unit = {}): Application {
    if (application is CoreApplication) {
        (application as CoreApplication).action()
    }
    return application
}

inline fun <reified Obj> Activity.hold(): Obj {
    return (application as CoreApplication).holdGet(Obj::class.java)
}

fun Fragment.core(action: CoreApplication.() -> Unit = {}): Application {
    return requireActivity().core(action)
}

inline fun <reified Obj> Fragment.hold(): Obj {
    return (requireActivity().application as CoreApplication).holdGet(Obj::class.java)
}

//</editor-fold desc="Application级别的单例模式">

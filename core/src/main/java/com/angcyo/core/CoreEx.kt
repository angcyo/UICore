package com.angcyo.core

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.angcyo.library.app
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.find
import com.angcyo.widget.text.DslTextView

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

/**[CoreApplication]中的[ViewModel]*/
inline fun <reified VM : ViewModel> Activity.vmCore(): VM {
    return ViewModelProvider(
        core() as CoreApplication,
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    ).get(VM::class.java)
}

/**返回CoreApplication级别的[ViewModel]*/
inline fun <reified VM : ViewModel> Fragment.vmCore(): VM {
    return requireActivity().vmCore()
}

/**返回CoreApplication级别的[ViewModel]*/
inline fun <reified VM : ViewModel> vmCore(): VM {
    return ViewModelProvider(
        app() as CoreApplication,
        ViewModelProvider.AndroidViewModelFactory.getInstance(app())
    ).get(VM::class.java)
}

//</editor-fold desc="Application级别的单例模式">

fun DslGroupHelper.appendTextItem(
    attachToRoot: Boolean = true,
    action: DslTextView.() -> Unit
): View? {
    return inflate(R.layout.lib_text_layout, attachToRoot) {
        find<DslTextView>(R.id.lib_text_view)?.apply {
            this.action()
        }
    }
}

fun DslGroupHelper.appendItem(
    @LayoutRes
    layoutId: Int = R.layout.lib_text_layout,
    attachToRoot: Boolean = true,
    action: View.() -> Unit
): View? {
    return inflate(layoutId, attachToRoot) {
        this.action()
    }
}

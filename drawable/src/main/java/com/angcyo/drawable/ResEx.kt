package com.angcyo.drawable

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.angcyo.library.app

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**获取状态栏高度*/
fun Context.getStatusBarHeight(): Int {
    val resources = resources
    var result = 0
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

/**
 * 导航栏高度
 *
 * @see [navBarHeight]
 */
fun Context.getNavBarHeight(): Int {
    val resources = resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

/**
 * 显示的导航栏高度
 */
fun Context.navBarHeight(): Int {
    var result = 0
    if (this is Activity) {
        val decorRect = Rect()
        val windowRect = Rect()
        window.decorView.getGlobalVisibleRect(decorRect)
        window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            .getGlobalVisibleRect(windowRect)
        result = if (decorRect.width() > decorRect.height()) { //横屏
            decorRect.width() - windowRect.width()
        } else { //竖屏
            decorRect.bottom - windowRect.bottom
        }
    }
    return result
}

@ColorInt
fun Context.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

@Px
fun Context.getDimen(@DimenRes id: Int): Int {
    return resources.getDimensionPixelOffset(id)
}

@ColorInt
fun getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(app(), id)
}

@Px
fun getDimen(@DimenRes id: Int): Int {
    return app().getDimen(id)
}

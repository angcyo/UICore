package com.angcyo.library.ex

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

/**
 * ContentView 的高度, 包含 DecorView的高度-状态栏-导航栏
 *
 *
 * 当状态栏是透明时, 那么状态栏的高度会是0
 */
fun Context.getContentViewHeight(): Int {
    if (this is Activity) {
        val window = this.window
        return window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            .measuredHeight
    }
    return 0
}
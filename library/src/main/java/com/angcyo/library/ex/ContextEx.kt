package com.angcyo.library.ex

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat

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

/**是否有自定的权限*/
fun Context.havePermissions(permissions: Array<out String>): Boolean {
    //所有权限都允许
    var granted = true
    for (p in permissions) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            granted = false
            break
        }
    }
    return granted
}
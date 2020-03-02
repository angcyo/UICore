package com.angcyo.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.angcyo.DslAHelper
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.fragment.FragmentBridge
import com.angcyo.fragment.R
import com.angcyo.library.ex.havePermissions

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**激活布局全屏*/
fun Activity.enableLayoutFullScreen(enable: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color))
        setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color))
        val decorView = window.decorView
        var systemUiVisibility = decorView.systemUiVisibility
        if (enable) { //https://blog.csdn.net/xiaonaihe/article/details/54929504
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE /*沉浸式, 用户显示状态, 不会清楚原来的状态*/
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        } else {
            systemUiVisibility =
                systemUiVisibility.remove(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = systemUiVisibility
        }
    }
}

fun Activity.moveToBack(nonRoot: Boolean = true): Boolean {
    return moveTaskToBack(nonRoot)
}

/**拦截TouchEvent*/
fun Context.interceptTouchEvent(intercept: Boolean = true) {
    if (this is BaseAppCompatActivity) {
        this.interceptTouchEvent = intercept
    }
}

/**设置状态栏颜色*/
fun Activity.setStatusBarColor(color: Int = Color.TRANSPARENT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = color
    }
}

/**设置导航栏颜色*/
fun Activity.setNavigationBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.navigationBarColor = color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = color
        }
    }
}

/**半透明/全透明 状态栏
 * https://www.jianshu.com/p/add47d6bde29*/
fun Activity.translucentStatusBar(full: Boolean = false) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    if (full) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setStatusBarColor(Color.TRANSPARENT)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    } else {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

/**半透明/全透明 导航栏
 * https://www.jianshu.com/p/add47d6bde29*/
fun Activity.translucentNavigationBar(full: Boolean = false) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    if (full) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        setNavigationBarColor(Color.TRANSPARENT)
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    } else {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}

private fun Int.remove(value: Int): Int = this and value.inv()

/**开始[Window]转场动画, 请调用[transition]*/
fun Context.dslAHelper(action: DslAHelper.() -> Unit) {
    DslAHelper(this).apply {
        this.action()
        doIt()
    }
}

/**返回true, 表示可以关闭界面*/
fun ComponentActivity.checkBackPressedDispatcher(): Boolean {
    if (onBackPressedDispatcher.hasEnabledCallbacks()) {
        onBackPressedDispatcher.onBackPressed()
        return false
    }
    return true
}

//<editor-fold desc="权限相关">

fun Activity.requestPermission(
    permissions: List<String>,
    requestCode: Int = FragmentBridge.generateCode()
) {
    requestPermission(permissions.toTypedArray(), requestCode)
}

fun Activity.requestPermission(
    permissions: Array<out String>,
    requestCode: Int = FragmentBridge.generateCode()
) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

fun FragmentActivity.checkAndRequestPermission(
    permissions: List<String>,
    onPermissionGranted: () -> Unit = {}
) {
    checkAndRequestPermission(permissions.toTypedArray(), onPermissionGranted)
}

/**检查或者请求权限*/
fun FragmentActivity.checkAndRequestPermission(
    permissions: Array<out String>,
    onPermissionGranted: () -> Unit = {}
) {
    if (havePermissions(*permissions)) {
        onPermissionGranted()
    } else {
        //请求权限
        FragmentBridge.install(supportFragmentManager).run {
            startRequestPermissions(permissions) { permissions, _ ->
                if (havePermissions(*permissions)) {
                    onPermissionGranted()
                }
            }
        }
    }
}

//</editor-fold desc="权限相关">
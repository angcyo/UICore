package com.angcyo.activity

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.angcyo.DslAHelper
import com.angcyo.fragment.R

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

fun Context.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

/**设置状态栏颜色*/
fun Activity.setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = color
    }
}

private fun Int.remove(value: Int): Int = this and value.inv()

fun Activity.dslAHelper(action: DslAHelper.() -> Unit) {
    DslAHelper(this).apply {
        this.action()
        doIt()
    }
}

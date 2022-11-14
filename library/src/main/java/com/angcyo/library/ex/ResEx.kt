package com.angcyo.library.ex

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.LocaleList
import android.view.View
import android.view.Window
import androidx.annotation.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.angcyo.library.L
import com.angcyo.library.PlaceholderApplication
import com.angcyo.library.app
import com.angcyo.library.component.defaultDensityAdapter
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**获取状态栏定义的高度*/
fun Context.getStatusBarHeight(): Int {
    return defaultDensityAdapter {
        val resources = resources
        var result = 0
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId != 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        result
    }
}

/**
 * 导航栏定义的高度, 并非当前显示的高度
 * @see [navBarHeight]
 */
fun Context.getNavBarHeight(): Int {
    return defaultDensityAdapter {
        val resources = resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}

/** 显示的导航栏高度 */
fun Context.navBarHeight(): Int {
    var result = 0
    if (this is Activity) {
        val decorRect = Rect()
        val windowRect = Rect()
        window.decorView.getGlobalVisibleRect(decorRect)
        window.findViewById<View>(Window.ID_ANDROID_CONTENT).getGlobalVisibleRect(windowRect)
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

@ColorInt
fun Context.loadColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

@Px
fun Context.getDimen(@DimenRes id: Int): Int {
    return resources.getDimensionPixelOffset(id)
}

@Px
fun Context.loadDimen(@DimenRes id: Int): Int {
    return resources.getDimensionPixelOffset(id)
}

fun Context.loadDrawable(id: Int): Drawable? {
    if (id <= 0) {
        return null
    }
    return try {
        ContextCompat.getDrawable(this, id)?.initBounds()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

@ColorInt
fun getColor(@ColorRes id: Int, context: Context = app()): Int {
    if (context is PlaceholderApplication) {
        return Color.YELLOW
    }
    return ContextCompat.getColor(context, id)
}

@Px
fun getDimen(@DimenRes id: Int, context: Context = app()): Int {
    return context.getDimen(id)
}

fun getInteger(@IntegerRes id: Int, context: Context = app()): Int {
    return context.resources.getInteger(id)
}

@ColorInt
fun _color(@ColorRes id: Int, context: Context = app()): Int {
    return getColor(id, context)
}

@Px
fun _dimen(@DimenRes id: Int, context: Context = app()): Int {
    return getDimen(id, context)
}

fun _integer(@IntegerRes id: Int, context: Context = app()): Int {
    return getInteger(id, context)
}

fun _drawable(@DrawableRes id: Int, context: Context = app()): Drawable? {
    return context.loadDrawable(id)
}

/**判断[Int]是否是资源的id*/
fun Int.isRes() = this.have(0x7F000000)

fun _colorDrawable(color: Int): Drawable {
    return if (color.isRes()) {
        ColorDrawable(_color(color))
    } else {
        ColorDrawable(color)
    }
}

fun _string(@StringRes id: Int): String {
    return app().resources.getString(id)
}

fun _string(@StringRes id: Int, vararg formatArgs: Any): String {
    return app().resources.getString(id, *formatArgs)
}

fun _stringArray(@ArrayRes id: Int): Array<String> {
    return app().resources.getStringArray(id)
}

/**获取指定语种的字符串资源*/
fun _string(@StringRes id: Int, locale: Locale?): String {
    return localResources(app(), locale).getString(id)
}

fun _stringArray(@ArrayRes id: Int, locale: Locale?): Array<String> {
    return localResources(app(), locale).getStringArray(id)
}

/**获取某个语种下的 Resources对象*/
fun localResources(context: Context, locale: Locale?): Resources {
    val config = Configuration()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
    } else {
        config.locale = locale
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        context.createConfigurationContext(config).resources
    } else Resources(context.assets, context.resources.displayMetrics, config)
}

/**是否是暗黑模式*/
fun isDarkMode(context: Context = app()): Boolean {
    return when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> isDarkModeOnSystem(context)
        else -> false
    }
}

fun isDarkModeOnSystem(context: Context = app()): Boolean {
    return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false
    }
}
package com.angcyo.library

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Looper
import com.angcyo.library.ex.isDebug
import com.orhanobut.hawk.Hawk

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Library {

    var application: Application? = null
    var debug: Boolean = isDebug()

    fun init(context: Application, debug: Boolean = isDebug()) {
        application = context
        Library.debug = debug

        /*sp持久化库*/
        Hawk.init(context)
            .build()
    }
}

fun app(): Application = Library.application!!

/**
 * 获取APP的名字
 */
fun Context.getAppName(): String {
    var appName = packageName
    val packageManager = packageManager
    val packInfo: PackageInfo
    try {
        packInfo = packageManager.getPackageInfo(appName, 0)
        appName = packInfo.applicationInfo.loadLabel(packageManager).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return appName
}

/**
 * 返回app的版本名称.
 *
 * @param context the context
 * @return app version name
 */
fun Context.getAppVersionName(): String {
    var version = "unknown"
    // 获取package manager的实例
    val packageManager = packageManager
    // getPackageName()是你当前类的包名，0代表是获取版本信息
    val packInfo: PackageInfo
    try {
        packInfo = packageManager.getPackageInfo(
            packageName,
            0
        )
        version = packInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    // Log.i("版本名称:", version);
    return version
}

/**
 * 返回app的版本代码.
 *
 * @param context the context
 * @return app version code
 */
fun Context.getAppVersionCode(): Int { // 获取package manager的实例
    val packageManager = packageManager
    // getPackageName()是你当前类的包名，0代表是获取版本信息
    var code = 1
    val packInfo: PackageInfo
    try {
        packInfo = packageManager.getPackageInfo(
            packageName,
            0
        )
        code = packInfo.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    // Log.i("版本代码:", version);
    return code
}

/**根据资源名字, 资源类型, 返回资源id*/
fun getId(name: String, type: String): Int {
    return getIdentifier(name, type)
}

fun getIdentifier(name: String, type: String): Int {
    //(name, string)
    return app().resources.getIdentifier(name, type, app().packageName)
}

fun getAppString(name: String): String? {
    val id = getId(name, "string")
    return if (id == 0) null else app().resources.getString(id)
}

fun getAppColor(name: String): Int {
    val id = getId(name, "color")
    return when {
        id == 0 -> Color.TRANSPARENT
        Build.VERSION.SDK_INT >= 23 -> app().getColor(id)
        else -> app().resources.getColor(id)
    }
}

fun getAppName(): String {
    return app().getAppName()
}

fun getAppVersionName(): String {
    return app().getAppVersionName()
}

fun getAppVersionCode(): Int {
    return app().getAppVersionCode()
}

fun getScreenWidth() = app().resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun getScreenHeight() = app().resources.displayMetrics.heightPixels

fun getStatusBarHeight() = app().getStatusBarHeight()

/**获取状态栏高度*/
private fun Context.getStatusBarHeight(): Int {
    val resources = resources
    var result = 0
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

/**是否是主线程*/
fun isMain() = Looper.getMainLooper() == Looper.myLooper()
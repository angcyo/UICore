package com.angcyo.library

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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

    lateinit var application: Application
    var debug: Boolean = isDebug()

    fun init(context: Application, debug: Boolean = isDebug()) {
        application = context
        Library.debug = debug

        /*sp持久化库*/
        Hawk.init(context)
            .build()
    }
}

fun app(): Application = Library.application

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

fun getAppName(): String {
    return app().getAppName()
}

fun getAppVersionName(): String {
    return app().getAppVersionName()
}

fun getAppVersionCode(): Int {
    return app().getAppVersionCode()
}
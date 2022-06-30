package com.angcyo.library

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.ex.*
import com.orhanobut.hawk.Hawk
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Library {

    var application: Context? = null
    var debug: Boolean = isDebug()

    /**初始化库[Application]*/
    fun init(context: Application, debug: Boolean = isDebug()) {
        application = context
        Library.debug = debug

        initHawk(context)
    }

    /**初始化[Hawk]*/
    fun initHawk(context: Context) {
        /*sp持久化库*/
        try {
            if (!Hawk.isBuilt()) {
                Hawk.init(context)
                    .build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Hawk.init(context)
                .build()
        }
    }
}

fun app(): Context = Library.application
    ?: (LibInitProvider.contentProvider)?.apply {
        Library.initHawk(this)
    }
    ?: currentApplication()?.apply {
        Library.initHawk(this)
    }
    ?: PlaceholderApplication().apply {
        Log.e("PlaceholderApplication", "application 未初始化")
    }

fun appLifecycleOwner(): LifecycleOwner? =
    if (Library.application is LifecycleOwner) Library.application as LifecycleOwner else null

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
 * 获取APP的图标
 */
fun Context.getAppIcon(): Drawable? {
    val appName = packageName
    val packageManager = packageManager
    try {
        val packInfo = packageManager.getPackageInfo(appName, 0)
        return packInfo.applicationInfo.loadIcon(packageManager)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return null
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

fun Context.getId(name: String, type: String): Int {
    return getIdentifier(name, type)
}

fun getIdentifier(name: String, type: String): Int {
    //(name, string)
    return app().getIdentifier(name, type)
}

fun Context.getIdentifier(name: String, type: String): Int {
    return resources.getIdentifier(name, type, packageName)
}

fun getAppString(name: String): String? {
    val id = getId(name, "string")
    return if (id == 0) null else app().resources.getString(id)
}

fun getAppBoolean(name: String): Boolean? {
    val id = getId(name, "bool")
    return if (id == 0) null else app().resources.getBoolean(id)
}

fun Context.getAppString(name: String): String? {
    val id = getId(name, "string")
    return if (id == 0) null else resources.getString(id)
}

fun getAppColor(name: String): Int {
    val id = getId(name, "color")
    return when {
        id == 0 -> Color.TRANSPARENT
        Build.VERSION.SDK_INT >= 23 -> app().getColor(id)
        else -> app().resources.getColor(id)
    }
}

fun Context.getAppColor(name: String): Int {
    val id = getId(name, "color")
    return when {
        id == 0 -> Color.TRANSPARENT
        Build.VERSION.SDK_INT >= 23 -> getColor(id)
        else -> resources.getColor(id)
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

/**排除了显示的状态栏高度和导航栏高度*/
val _screenWidth: Int get() = app().getScreenWidth()
val _screenHeight: Int get() = app().getScreenHeight()
val _statusBarHeight: Int get() = app().getStatusBarHeight()
val _navBarHeight: Int get() = app().getNavBarHeight()

fun View.getScreenWidth() = resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun View.getScreenHeight() = resources.displayMetrics.heightPixels

fun Context.getScreenWidth() = resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun Context.getScreenHeight() = resources.displayMetrics.heightPixels

/**导航栏正在显示的高度*/
fun Context?.getNavBarHeightShow() = activityContent()?.navBarHeight() ?: 0

/**是否是主线程*/
fun isMain() = Looper.getMainLooper() == Looper.myLooper()

/**激活组件*/
fun enableComponent(componentClass: Class<*>, enable: Boolean = true, content: Context = app()) {
    val component = ComponentName(content, componentClass)
    val packageManager = content.packageManager
    val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
}

/**防止自定义[View]中, 使用[Library.application]崩溃的问题*/
fun View.attachInEditMode() {
    if (isInEditMode) {
        Library.application = context.applicationContext
    }
}

/**获取存储文件的基础路径
 * [folderName] 文件夹的名字
 *
 * [android.content.Context.getFilesDir] [/data/user/0/com.angcyo.uicore.demo/files/]
 * [android.content.Context.getExternalFilesDir] [/storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/]
 *
 * [com.angcyo.core.component.file.DslFileHelperKt.appFilePath]
 * [com.angcyo.library.utils.FileUtilsKt.appFolderPath]
 * */
fun libFolderPath(folderName: String, context: Context = app()): String {
    val folderFile = context.getExternalFilesDir(folderName) ?: File(context.filesDir, folderName)
    if (!folderFile.exists()) {
        folderFile.mkdirs()
    }
    return folderFile.absolutePath
}

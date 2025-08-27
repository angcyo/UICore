package com.angcyo.library

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.annotation.CallComplianceAfter
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.BackgroundThread
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.*
import com.angcyo.library.utils.FileUtils.appRootExternalFolder
import com.angcyo.library.utils.fileNameUUID
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

        //双线程模式
        BackgroundThread.instance
    }

    /**[Hawk]文件存储的路径*/
    var hawkPath: String? = null

    /**初始化[Hawk]*/
    fun initHawk(context: Context) {
        /*sp持久化库*/
        fun _initHawk() {
            Hawk.init(context).build()
            val path = "/shared_prefs/Hawk2.xml"
            val hawkXmlFile = File(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.dataDir
                } else {
                    context.filesDir.parentFile
                }, path
            )
            if (hawkXmlFile.exists()) {
                hawkPath = hawkXmlFile.absolutePath
            }
        }
        try {
            if (!Hawk.isBuilt()) {
                _initHawk()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _initHawk()
        }
    }

    //region ---常量---

    /**点击次数*/
    var CLICK_COUNT = 0

    /**强制标识Debug*/
    var isDebugTypeVal = false

    //endregion ---常量---

    //region ---ccs---

    /**是否是激光啄木鸟app*/
    fun isLaserPeckerApp(): Boolean {
        val context = lastContext
        val packageName = context.packageName
        return packageName.have("com.hingin.*.hiprint")
        //return packageName == "com.hingin.l1.hiprint" || packageName == "com.hingin.lp1.hiprint" || packageName == "com.hingin.rn.hiprint"
    }

    /**2025-1-8 是否是联想oem软件*/
    fun isLenovoOemApp(): Boolean {
        val context = lastContext
        val packageName = context.packageName
        return packageName == "com.hingin.lp1.hiprint.lenovo" /*|| isDebugType()*/
    }

    /**签名检查*/
    @Keep
    @JvmStatic
    @CallComplianceAfter
    fun ccs(c: String?): Boolean {
        if (c.isNullOrBlank()) {
            return true
        }
        val context = lastContext
        val md5 = context.getAppSignatureMD5()
        if (isLaserPeckerApp()) {
            if (md5 == c) {
                return true
            }
            libCacheFile("ccs").writeText("app:$md5\nccs:$c", false)
            return false
        }
        return true
    }

    //endregion ---ccs---
}

fun app(): Context = Library.application ?: (LibInitProvider.contentProvider)?.apply {
    Library.initHawk(this)
} ?: currentApplication()?.apply {
    Library.initHawk(this)
} ?: PlaceholderApplication().apply {
    Log.e("PlaceholderApplication", "application 未初始化")
}

/**Ide编辑模式*/
val isInEditMode: Boolean
    get() = app().isPlaceholderApplication()

fun Context?.isPlaceholderApplication() = this is PlaceholderApplication

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
        appName = packInfo.applicationInfo?.loadLabel(packageManager).toString()
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
        return packInfo.applicationInfo?.loadIcon(packageManager)
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
            packageName, 0
        )
        version = packInfo?.versionName ?: ""
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
@CallComplianceAfter
fun Context.getAppVersionCode(): Long { // 获取package manager的实例
    val packageManager = packageManager
    // getPackageName()是你当前类的包名，0代表是获取版本信息
    var code = 1L
    val packInfo: PackageInfo
    try {
        packInfo = packageManager.getPackageInfo(packageName, 0)
        code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packInfo.longVersionCode
        } else {
            packInfo.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    // Log.i("版本代码:", version);
    return code
}

/**根据资源名字, 资源类型, 返回资源id
 * [getAppBoolean]
 * [getAppString]
 * */
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
    return if (id == 0) null else _string(id)
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

fun getAppVersionCode(): Long {
    return app().getAppVersionCode()
}

/**排除了显示的状态栏高度和导航栏高度*/
val _screenWidth: Int get() = app().getScreenWidth()
val _screenHeight: Int get() = app().getScreenHeight()
val _statusBarHeight: Int get() = app().getStatusBarHeight()
val _navBarHeight: Int get() = app().getNavBarHeight()

/**手机底部插入的装饰高度, 比如导航栏的高度, 导航条的高度*/
val _bottomInset: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        lastContext.getInsetsType(WindowInsets.Type.navigationBars())?.bottom ?: 0
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        lastContext.getWindowInsets()?.systemWindowInsetBottom ?: 0
        //和stableInsetBottom值一样
    } else {
        _navBarHeight
    }

/**[_bottomInset]*/
val _topInset: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        lastContext.getInsetsType(WindowInsets.Type.statusBars())?.top ?: 0
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        lastContext.getWindowInsets()?.systemWindowInsetTop ?: 0
        //和stableInsetTop值一样
    } else {
        _navBarHeight
    }

/**获取屏幕圆角半径*/
@Pixel
val _screenCornerRadius: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        lastContext.getRoundedCorner()?.radius ?: 0
    } else {
        0
    }

/**获取当前设备的刷新帧率[60] [90] [120]*/
val _refreshRate: Float
    get() {
        val windowManager = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mode = windowManager.defaultDisplay.mode
            mode.refreshRate
        } else {
            60f
        }
    }

/**刷新率缩放的倍数, 比如120fps相对于60fps, 就是2倍, 动画时长就要放大2倍, 动画步长就要缩小2倍*/
val _refreshRateRatio: Float get() = _refreshRate / 60f

/**保持不同设备的不同刷新率一致性*/
val Float.rr
    get() = this / _refreshRateRatio

/**屏幕实际的大小, 而不是排除了导航栏/状态栏*/
val _realSize: Point
    get() {
        val windowManager = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.defaultDisplay.getRealSize(point)
            //app().display.getRealSize(point)
        } else {
            point.x = _screenWidth
            point.y = _screenHeight
        }
        return point
    }

fun View.getScreenWidth() = resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun View.getScreenHeight() = resources.displayMetrics.heightPixels

fun Context.getScreenWidth() = resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun Context.getScreenHeight() = resources.displayMetrics.heightPixels

/**导航栏正在显示的高度*/
fun Context?.getNavBarHeightShow() = activityContent()?.navBarHeight() ?: 0

/**获取屏幕圆角信息
 * https://developer.android.com/guide/topics/ui/look-and-feel/rounded-corners?hl=zh-cn
 *
 * Only visual Contexts (such as Activity or one created with Context#createWindowContext) or
 * ones created with Context#createDisplayContext are associated with displays.*/
fun Context.getRoundedCorner(position: Int? = null) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            display?.getRoundedCorner(position ?: RoundedCorner.POSITION_TOP_LEFT)
        } catch (e: Exception) {
            //e.printStackTrace()
            L.w("异常操作:${e.message}")
            null
        }
    } else {
        null
    }

/**[WindowInsets]*/
@RequiresApi(Build.VERSION_CODES.M)
fun Context.getWindowInsets(view: View? = null): WindowInsets? {
    val targetView = view ?: if (this is Activity) {
        window.decorView
    } else {
        null
    } ?: return null
    return targetView.rootWindowInsets
}

@RequiresApi(Build.VERSION_CODES.R)
fun Context.getInsetsType(
    typeMask: Int = WindowInsets.Type.statusBars(), view: View? = null
): Insets? {
    return getWindowInsets(view)?.getInsets(typeMask)
}

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
        try {
            Library.application = context.applicationContext
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**获取存储文件的基础路径, 可以在应用详情中, 通过清理存储清除.
 * [folderName] 文件夹的名字
 *
 * [android.content.Context.getFilesDir] [/data/user/0/com.angcyo.uicore.demo/files/]
 * [android.content.Context.getExternalFilesDir] [/storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/]
 *
 * [com.angcyo.core.component.file.appFilePath]
 * [com.angcyo.library.utils.appFolderPath]
 * */
fun libFileFolder(folderName: String = "", context: Context = app()): File {
    val folderFile = context.getExternalFilesDir(folderName) ?: File(context.filesDir, folderName)
    if (!folderFile.exists()) {
        folderFile.mkdirs()
    }
    return folderFile
}

fun libFolderPath(folderName: String = "", context: Context = app()): String {
    return libFileFolder(folderName, context).absolutePath
}

/**缓存目录, 可以在应用详情中, 通过清理缓存清除.*/
fun libCacheFolderPath(folderName: String = "", context: Context = app()): String {
    val folderFile = File(context.externalCacheDir ?: context.cacheDir, folderName)
    if (!folderFile.exists()) {
        folderFile.mkdirs()
    }
    return folderFile.absolutePath
}

/**获取一个存储文件*/
fun libFile(name: String = fileNameUUID(), folderName: String = ""): File {
    return File(libFolderPath(folderName), name)
}

/**[libAppFolderFile]*/
fun libAppFile(name: String = fileNameUUID(), folderName: String = ""): File =
    File(libAppFolderFile(folderName), name)

/**获取一个缓存文件*/
fun libCacheFile(name: String = fileNameUUID(), folderName: String = ""): File {
    return File(libCacheFolderPath(folderName), name)
}

/**获取一个app文件目录文件对象*/
fun libAppFolderFile(folderName: String = ""): File {
    return appRootExternalFolder(folderName)
}

/**获取一个缓存目录文件对象*/
fun libCacheFolderFile(folderName: String = ""): File {
    return File(libCacheFolderPath(folderName))
}
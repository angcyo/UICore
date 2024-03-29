package com.angcyo.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.angcyo.DslAHelper
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.fragment.*
import com.angcyo.layout.FragmentSwipeBackLayout
import com.angcyo.library.ex._color
import com.angcyo.library.ex.have
import com.angcyo.library.ex.havePermissions
import com.angcyo.library.utils.storage.haveSdCardManagePermission
import com.angcyo.library.utils.storage.haveSdCardPermission
import com.angcyo.library.utils.storage.sdCardPermission

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**激活布局全屏*/
fun Activity.enableLayoutFullScreen(enable: Boolean = true) {
    window.enableLayoutFullScreen(enable)
}

fun Window.enableLayoutFullScreen(enable: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //去掉半透明状态栏
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //去掉半透明导航栏
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val decorView = decorView
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //https://www.jianshu.com/p/2f7ac7a05c30
            //支持刘海屏
            //https://developer.android.google.cn/guide/topics/display-cutout

            //val windowInsets = getDecorView().rootView.rootWindowInsets

            val lp: WindowManager.LayoutParams = attributes
            if (enable) {
                //https://developer.android.google.cn/guide/topics/display-cutout#choose_how_your_app_handles_cutout_areas
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
            attributes = lp
        }
    }
}

/**全屏模式*/
fun Activity.fullscreen(enable: Boolean = true) {
    window.fullscreen(enable)
}

fun Window.fullscreen(enable: Boolean = true) {
    decorView.fullscreen(enable)
}

fun View.fullscreen(enable: Boolean = true) {
    systemUiVisibility = if (enable) {
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    } else {
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

/** 是否是白色状态栏. 如果是, 那么系统的状态栏字体会是灰色
 * 请在[super.onCreate]调用之后触发
 * */
fun Activity.lightStatusBar(light: Boolean = true) {
    try {
        window.lightStatusBar(light)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Window.lightStatusBar(light: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        //android 11
        if (light) {
            insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            insetsController?.setSystemBarsAppearance(0, 0)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //android 6
        val decorView = decorView
        val systemUiVisibility = decorView.systemUiVisibility
        if (light) {
            if (systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            if (!systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

fun Activity.lightNavigationBar(light: Boolean = true) {
    window.lightNavigationBar(light)
}

/**变暗状态栏,画出来的背景颜色*/
fun Activity.dimStatusBar(dim: Boolean = true, color: Int = _color(R.color.lib_status_bar_dim)) {
    findViewById<FragmentSwipeBackLayout>(R.id.fragment_container)?.apply {
        setDimStatusBar(dim, color)
    }
}

/**是否为白色导航栏*/
fun Window.lightNavigationBar(light: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        //android 11 //30
        if (light) {
            insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            insetsController?.setSystemBarsAppearance(0, 0)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //android 8 //26
        val decorView = decorView
        val systemUiVisibility = decorView.systemUiVisibility
        if (light) {
            if (systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            if (!systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }
}

/**[Window.ID_ANDROID_CONTENT]*/
fun Window.contentView(): FrameLayout =
    findViewById(Window.ID_ANDROID_CONTENT)

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
    window.translucentStatusBar(full)
}

fun Window.translucentStatusBar(full: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    if (full) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBarColor = Color.TRANSPARENT//全透明
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
}

/**半透明/全透明 导航栏
 * https://www.jianshu.com/p/add47d6bde29*/
fun Activity.translucentNavigationBar(full: Boolean = false) {
    window.translucentNavigationBar(full)
}

fun Window.translucentNavigationBar(full: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    if (full) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigationBarColor = Color.TRANSPARENT//全透明
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }
}

/**使用最高帧率
 * https://zhuanlan.zhihu.com/p/150011773*/
fun Window.enableHighRefresh() {
    /*
        M 是 6.0，6.0修改了新的api，并且就已经支持修改window的刷新率了。
        但是6.0那会儿，也没什么手机支持高刷新率吧，所以也没什么人注意它。
        我更倾向于直接判断 O，也就是 Android 8.0，我觉得这个时候支持高刷新率的手机已经开始了。
        */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // 获取系统window支持的模式
        val modes = windowManager.defaultDisplay.supportedModes
        // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
        modes.sortBy {
            it.refreshRate
        }
        val lp = attributes
        // 取出最大的那一个刷新率，直接设置给window
        lp.preferredDisplayModeId = modes.last().modeId
        attributes = lp
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

/**返回true, 表示可以关闭界面*/
fun AbsLifecycleFragment.checkBackPressedDispatcher(): Boolean {
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
    onPermissionGranted: (grant: Boolean) -> Unit = {}
) {
    checkAndRequestPermission(permissions.toTypedArray(), onPermissionGranted)
}

/**检查或者请求权限*/
fun FragmentActivity.checkAndRequestPermission(
    permissions: Array<out String>,
    onPermissionGranted: (grant: Boolean) -> Unit = {}
) {
    if (havePermissions(*permissions)) {
        onPermissionGranted(true)
    } else {
        //请求权限
        FragmentBridge.install(supportFragmentManager).run {
            startRequestPermissions(permissions) { permissions, grantResults ->
                onPermissionGranted(havePermissions(*permissions))
            }
        }
    }
}

/**请求SD卡权限*/
fun Context.requestSdCardPermission(
    permissionList: List<String> = sdCardPermission(),
    result: (granted: Boolean) -> Unit
) {
    if (haveSdCardPermission(permissionList, this)) {
        result(true)
        return
    }
    requestPermissions(permissionList) {
        result(it)
    }
}

/**请求SD卡管理权限*/
fun Context.requestSdCardManagePermission(result: (granted: Boolean) -> Unit) {
    if (haveSdCardManagePermission(context = this)) {
        result(true)
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val uri = Uri.parse("package:${packageName}")
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        if (this is FragmentActivity) {
            requestActivityResult(supportFragmentManager, intent) { resultCode, data ->
                result(haveSdCardManagePermission(context = this))
            }
        } else {
            startActivity(intent)
        }
    } else {
        requestPermissions(sdCardPermission()) {
            result(it)
        }
    }
}

//</editor-fold desc="权限相关">
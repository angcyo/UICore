package com.angcyo.library.component.pad

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.angcyo.library.app
import com.angcyo.library.utils.SystemPropertiesProxy
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 平板相关工具
 *
 * 判断是否在分屏模式
 * ```
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
 *   isInMultiWindowMode = activity.isInMultiWindowMode
 * }
 * ```
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/13
 */
object Pad {

    /**是否是平板模式~*/
    val isPad: Boolean
        get() = isTabletDevice

    /**只要判断是屏幕大小大于等于7.0英寸的设备就是平板了
     * https://blog.csdn.net/Fantasy_Lin_/article/details/111828002
     *
     * [min] 英寸
     *
     * ```
     * app().display
     * ```
     * Unable to create application com.angcyo.uicore.App: java.lang.UnsupportedOperationException:
     * Tried to obtain display from a Context not associated with one.
     * Only visual Contexts (such as Activity or one created with Context#createWindowContext)
     * or ones created with Context#createDisplayContext are associated with displays.
     * Other types of Contexts are typically related to background entities and may return an arbitrary display.
     * */
    fun isPadSize(min: Double = 7.0): Boolean {
        val wm = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val dm = DisplayMetrics()
        display?.getMetrics(dm)
        val x = (dm.widthPixels * 1.0 / dm.xdpi).pow(2.0)
        val y = (dm.heightPixels * 1.0 / dm.ydpi).pow(2.0)
        val c = sqrt(x + y)
        return c >= min
    }

    /** 判断是否平板设备，此值不会改变
     * https://juejin.cn/post/7046989823034261541
     */
    val isTabletDevice: Boolean by lazy {
        SystemPropertiesProxy[app(), "ro.build.characteristics"]?.contains("tablet", true) == true
    }

    /**
     * 动态判断是否平板窗口
     * 在平板设备上，也可能返回false。如分屏模式下
     * 如想判断物理设备是不是平板，请使用 isTabletDevice
     * @return true:平板,false:手机
     * @see isTabletDevice
     */
    fun isTabletWindow(context: Context = app()): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /** 平行窗口模式（华为、小米） */
    fun inMagicWindow(context: Context = app()): Boolean {
        val config: String = context.resources.configuration.toString()
        return config.contains("hwMultiwindow-magic") ||
                config.contains("miui-magic-windows") ||
                config.contains("hw-magic-windows")
    }

    /**判断是否在分屏模式
     * [android.app.Activity.onMultiWindowModeChanged]
     *
     * https://blog.csdn.net/qq_37658380/article/details/86630267
     * */
    fun isInMultiWindowMode(activity: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity is Activity) {
            return activity.isInMultiWindowMode
        }
        return false
    }

    /**
     * 窗口是横屏
     */
    fun isWindowLandscape(context: Context = app()): Boolean {
        val orientation: Int = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 设备是横屏
     */
    fun isDeviceLandscape(context: Context): Boolean {
        val screenPhysicsSize = getScreenPhysicsSize(context)
        return screenPhysicsSize.widthPixels > screenPhysicsSize.heightPixels
    }

    /**
     * 获取屏幕物理尺寸
     *
     * @param context 上下文
     * @return 物理尺寸
     */
    private fun getScreenPhysicsSize(context: Context): DisplayMetrics {
        val display = getDisplay(context)
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display?.getRealMetrics(dm)
        } else {
            display?.getMetrics(dm)
        }
        return dm
    }

    private fun getDisplay(context: Context): Display? {
        val windowManager = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= 30) {
            context.display!!
        } else {
            windowManager.defaultDisplay
        }
    }

    fun getScreenSize(context: Context): Point {
        val point = Point()
        val displayMetrics = context.resources.displayMetrics
        point.x = displayMetrics.widthPixels
        point.y = displayMetrics.heightPixels
        return point
    }
}
package com.angcyo.library

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

/**
 * 刘海屏手机: _screenHeight 获取到的高度不会包含状态栏的高度
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object Screen {

    /**屏幕可见区域宽度*/
    var visibleWidth: Int = _screenWidth

    /**屏幕可见区域高度, 包含状态栏和导航栏*/
    var visibleHeight: Int = _screenHeight

    /**内容区域的宽度*/
    var contentWidth: Int = _screenWidth

    /**内容区域的高度, 不包含导航栏*/
    var contentHeight: Int = _screenHeight

    val _metrics = DisplayMetrics()

    fun init(context: Context) {
        val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //DisplayMetrics{density=2.75, width=1080, height=2028, scaledDensity=2.75, xdpi=422.03, ydpi=422.204}
            //windowManager.defaultDisplay.getMetrics(metrics)

            //DisplayMetrics{density=2.75, width=1080, height=2158, scaledDensity=2.75, xdpi=422.03, ydpi=422.204}
            windowManager.defaultDisplay.getRealMetrics(_metrics)
            visibleWidth = _metrics.widthPixels
            visibleHeight = _metrics.heightPixels
        }

        if (context is Activity) {
            val contentView = context.window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            if (contentView.measuredWidth == 0 && contentView.measuredHeight == 0) {
                contentView.post {
                    contentWidth = contentView.measuredWidth
                    contentHeight = contentView.measuredHeight
                }
            } else {
                contentWidth = contentView.measuredWidth
                contentHeight = contentView.measuredHeight
            }
        } else {
            windowManager.defaultDisplay.getMetrics(_metrics)
            contentWidth = _metrics.widthPixels
            contentHeight = _metrics.heightPixels
        }
    }

    /**当屏幕旋转了, 但又没有调用init方法, 此时需要矫正一下数值*/
    fun _adjust() {
        if (_screenHeight > _screenWidth && visibleHeight > visibleWidth) {
            //不需要矫正
        } else {
            val oldWidth = visibleWidth
            val oldHeight = visibleHeight
            if (_screenHeight > _screenWidth) {
                //竖屏
                visibleWidth = min(oldHeight, oldWidth)
                visibleHeight = max(oldHeight, oldWidth)
            } else {
                //横屏
                visibleWidth = max(oldHeight, oldWidth)
                visibleHeight = min(oldHeight, oldWidth)
            }
        }
    }
}

val _visibleWidth: Int
    get() {
        Screen._adjust()
        return Screen.visibleWidth
    }

val _visibleHeight: Int
    get() {
        Screen._adjust()
        return Screen.visibleHeight
    }

val _contentWidth: Int
    get() {
        Screen._adjust()
        return Screen.contentWidth
    }

val _contentHeight: Int
    get() {
        Screen._adjust()
        return Screen.contentHeight
    }

/**导航栏是否显示*/
val _isNavigationBarShow: Boolean
    get() {
        return (_visibleWidth - _contentWidth) > _satusBarHeight ||
                (_visibleHeight - _contentHeight) > _satusBarHeight
    }


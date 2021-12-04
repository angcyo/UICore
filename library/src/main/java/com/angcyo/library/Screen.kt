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
    internal var visibleWidth: Int = _screenWidth

    /**屏幕可见区域高度, 包含状态栏和导航栏*/
    internal var visibleHeight: Int = _screenHeight

    /**内容区域的宽度*/
    internal var contentWidth: Int = _screenWidth

    /**内容区域的高度, 不包含导航栏*/
    internal var contentHeight: Int = _screenHeight

    internal var decorWidth: Int = _screenWidth
    internal var decorHeight: Int = _screenHeight

    internal val _metrics = DisplayMetrics()

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

            decorWidth = _metrics.widthPixels
            decorHeight = _metrics.heightPixels
        }

        if (context is Activity) {
            val decorView = context.window.decorView
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
            if (decorView.measuredWidth == 0 && decorView.measuredHeight == 0) {
                contentView.post {
                    decorWidth = decorView.measuredWidth
                    decorHeight = decorView.measuredHeight
                }
            } else {
                decorWidth = decorView.measuredWidth
                decorHeight = decorView.measuredHeight
            }
        } else {
            windowManager.defaultDisplay.getMetrics(_metrics)
            contentWidth = _metrics.widthPixels
            contentHeight = _metrics.heightPixels

            decorWidth = _metrics.widthPixels
            decorHeight = _metrics.heightPixels
        }
    }

    /**当屏幕旋转了, 但又没有调用init方法, 此时需要矫正一下数值*/
    fun _adjust() {
        if (_screenHeight > _screenWidth && visibleHeight > visibleWidth) {
            //不需要矫正
        } else {
            val oldWidth = visibleWidth
            val oldHeight = visibleHeight

            val oldDecorWidth = decorWidth
            val oldDecorHeight = decorHeight

            if (_screenHeight > _screenWidth) {
                //竖屏
                visibleWidth = min(oldHeight, oldWidth)
                visibleHeight = max(oldHeight, oldWidth)

                decorWidth = min(oldDecorHeight, oldDecorWidth)
                decorHeight = max(oldDecorHeight, oldDecorWidth)
            } else {
                //横屏
                visibleWidth = max(oldHeight, oldWidth)
                visibleHeight = min(oldHeight, oldWidth)

                decorWidth = max(oldDecorHeight, oldDecorWidth)
                decorHeight = min(oldDecorHeight, oldDecorWidth)
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

val _decorWidth: Int
    get() {
        Screen._adjust()
        return Screen.decorWidth
    }

val _decorHeight: Int
    get() {
        Screen._adjust()
        return Screen.decorHeight
    }

/**导航栏是否显示*/
val _isNavigationBarShow: Boolean
    get() {
        return (_visibleWidth - _contentWidth) > _statusBarHeight ||
                (_visibleHeight - _contentHeight) > _statusBarHeight
    }


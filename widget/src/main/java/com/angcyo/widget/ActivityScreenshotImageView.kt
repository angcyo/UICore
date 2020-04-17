package com.angcyo.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.angcyo.library.L
import com.angcyo.library.ex.activityContent
import com.angcyo.library.getNavBarHeightShow
import com.angcyo.widget.base.InvalidateProperty
import com.angcyo.widget.base.saveView

/**
 * 显示[Activity]的截图
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ActivityScreenshotImageView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    var radius: Float by InvalidateProperty(0f)

    val _path = Path()

    //Activity中的[decorView], 跟布局
    val _activityDecorView: View?
        get() {
            var ctx = context
            var i = 0
            while (i < 4 && ctx != null && ctx !is Activity && ctx is ContextWrapper) {
                ctx = ctx.baseContext
                i++
            }
            return if (ctx is Activity) {
                ctx.window.decorView
            } else {
                null
            }
        }

    init {
        //scaleType = ScaleType.CENTER_CROP

        val navBarHeightShow = context.activityContent().getNavBarHeightShow()

        //硬件加速
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        _activityDecorView?.apply {
            setImageBitmap(saveView()?.run {
                if (navBarHeightShow > 0) {
                    //去掉导航栏
                    //Bitmap.createBitmap(this, 0, 0, width, height - navBarHeightShow)
                    this
                } else {
                    this
                }
            })
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (width >= radius && height > radius) {
            _path.reset()
            _path.moveTo(radius, 0f)
            _path.lineTo(width - radius, 0f)
            _path.quadTo(width.toFloat(), 0f, width.toFloat(), radius)
            _path.lineTo(width.toFloat(), height - radius)
            _path.quadTo(width.toFloat(), height.toFloat(), width - radius, height.toFloat())
            _path.lineTo(radius, height.toFloat())
            _path.quadTo(0f, height.toFloat(), 0f, height - radius)
            _path.lineTo(0f, radius)
            _path.quadTo(0f, 0f, radius, 0f)
            canvas.clipPath(_path)
        }
        try {
            super.onDraw(canvas)
        } catch (e: Exception) {
            L.e("$e")
        }
    }
}
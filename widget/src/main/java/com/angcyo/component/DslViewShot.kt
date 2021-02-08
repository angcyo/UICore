package com.angcyo.component

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*

/**
 * layout xml 转成bitmap
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/08
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DslViewShot {
    @LayoutRes
    var layoutId: Int = -1

    var initLayout: (holder: DslViewHolder) -> Unit = {}

    var outWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    var outHeight = ViewGroup.LayoutParams.WRAP_CONTENT

    fun doIt(context: Context): Bitmap? {
        if (layoutId == -1) {
            L.e("布局文件不存在")
        }

        val rootView = LayoutInflater.from(context).inflate(layoutId, FrameLayout(context), false)
        initLayout(rootView.dslViewHolder())

        val widthMeasure = when (outWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> exactly(_screenWidth)
            ViewGroup.LayoutParams.WRAP_CONTENT -> atMost(_screenWidth)
            else -> atMost(outWidth)
        }

        val heightMeasure = when (outHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> exactly(_screenHeight)
            ViewGroup.LayoutParams.WRAP_CONTENT -> atMost(_screenHeight)
            else -> atMost(outHeight)
        }

        rootView.measure(widthMeasure, heightMeasure)
        rootView.layout(0, 0, rootView.mW(), rootView.mH())
        rootView.isDrawingCacheEnabled = true
        rootView.buildDrawingCache()
        return rootView.drawingCache
    }
}

/**自定义[xml]截图*/
fun Context.shot(action: DslViewShot.() -> Unit): Bitmap? {
    return DslViewShot().run {
        action()
        doIt(this@shot)
    }
}
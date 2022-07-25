package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Color
import com.angcyo.doodle.layer.BaseLayer

/**
 * 背景绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BackgroundElement(baseLayer: BaseLayer) : BaseElement(baseLayer) {

    /**背景颜色*/
    var backgroundColor: Int = Color.TRANSPARENT

    override fun onDraw(canvas: Canvas) {
        paint.color = backgroundColor
        canvas.drawRect(baseLayer.doodleDelegate.viewBox.contentRect, paint)
    }
}
package com.angcyo.doodle.element

import android.graphics.Canvas
import com.angcyo.doodle.R
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex._color

/**
 * 背景绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BackgroundElement : BaseElement() {

    /**背景颜色*/
    var backgroundColor: Int = _color(R.color.transparent20) //Color.TRANSPARENT

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        paint.color = backgroundColor
        canvas.drawRect(layer.doodleDelegate.viewBox.contentRect, paint)
    }
}
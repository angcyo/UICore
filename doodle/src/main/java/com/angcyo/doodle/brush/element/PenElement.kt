package com.angcyo.doodle.brush.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.dp

/**
 * 钢笔绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PenElement(val pointList: List<TouchPoint>) : BaseElement() {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        pointList.forEach {
            canvas.drawCircle(it.eventX, it.eventY, 3 * dp, paint)
        }
    }
}
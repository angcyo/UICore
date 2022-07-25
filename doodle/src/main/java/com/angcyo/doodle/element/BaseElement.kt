package com.angcyo.doodle.element

import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import com.angcyo.library.ex.dp

/**
 * 基础的绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseElement : IElement {

    /**画笔*/
    val paint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.BLACK
        this.style = Paint.Style.FILL
        textSize = 12 * dp
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

}
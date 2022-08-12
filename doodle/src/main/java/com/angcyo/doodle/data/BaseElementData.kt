package com.angcyo.doodle.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint

/**
 * 绘制元素, 需要的绘制数据信息
 *
 * [com.angcyo.doodle.core.DoodleConfig]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class BaseElementData : IElementData {

    /**笔的颜色*/
    var paintColor: Int = Color.TRANSPARENT

    /**笔的宽度, 理论上会等于[android.graphics.Paint.setStrokeWidth]*/
    var paintWidth: Float = 1f

    /**更新笔的样式*/
    fun updatePaint(paint: Paint) {
        //paint.style = Paint.Style.STROKE
        paint.strokeWidth = paintWidth
        paint.color = paintColor
    }

    /**定义图片画刷的图片*/
    var brushBitmap: Bitmap? = null

}
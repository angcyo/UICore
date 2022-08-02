package com.angcyo.doodle.element

import android.graphics.*
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 橡皮擦
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class EraserBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.let {
            paint.strokeWidth = brushElementData.paintWidth
            canvas.drawPath(it, paint)
        }
    }
}
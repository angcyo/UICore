package com.angcyo.doodle.element

import android.graphics.*
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer

/**
 * 钢笔绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class PenBrushElement(brushElementData: BrushElementData) :
    BaseBrushElement(brushElementData) {

    init {
        paint.style = Paint.Style.STROKE
    }

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)

        /*brushElementData.brushBitmap?.let {
            paint.shader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT).apply {
                setLocalMatrix(Matrix().apply {
                    postScale(
                        brushElementData.paintWidth / it.width,
                        brushElementData.paintWidth / it.height,
                        it.width / 2f, it.height / 2f
                    )
                })
            }
        }*/
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = brushElementData.paintWidth
            canvas.drawPath(it, paint)
        }
    }
}
package com.angcyo.canvas.core.renderer.items

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.items.TextItem
import com.angcyo.canvas.utils.createPaint
import com.angcyo.drawable.textHeight
import com.angcyo.drawable.textWidth
import com.angcyo.library.ex.dp

/**
 * 文本组件渲染
 * [TextItem]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TextItemRenderer(
    val textItem: TextItem,
    canvasViewBox: CanvasViewBox
) : BaseItemRenderer(canvasViewBox) {

    val paint = createPaint(Color.BLACK, Paint.Style.FILL).apply {
        //init
        textSize = 12 * dp
    }

    override fun onUpdateRendererBounds(canvasView: CanvasView) {
        super.onUpdateRendererBounds(canvasView)
        if (bounds.isEmpty) {
            bounds.set(0f, 0f, paint.textWidth(textItem.text), paint.textHeight())
        }
    }

    override fun render(canvas: Canvas) {
        val _bounds = bounds//canvasViewBox.matrix.mapRectF(bounds) //bounds

        /*canvas.withScale(
            canvasViewBox.matrix.getScaleX(),
            canvasViewBox.matrix.getScaleY(),
            _bounds.centerX(),
            _bounds.centerY()
        ) {
            canvas.drawText(
                textItem.text ?: "",
                _bounds.left,
                _bounds.bottom - paint.descent(),
                paint
            )
        }*/

//        canvas.withMatrix(transformer.transformerMatrix) {
        canvas.drawText(
            textItem.text ?: "",
            _bounds.left,
            _bounds.bottom - paint.descent(),
            paint
        )
//        }
    }

}
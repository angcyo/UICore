package com.angcyo.canvas.core.renderer.items

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.component.items.TextItem
import com.angcyo.canvas.utils.createPaint
import com.angcyo.drawable.textHeight
import com.angcyo.drawable.textWidth

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
    canvasViewBox: CanvasViewBox,
    transformer: Transformer = Transformer(canvasViewBox)
) : BaseItemRenderer(canvasViewBox, transformer) {

    val paint = createPaint(Color.BLACK, Paint.Style.FILL).apply {
        textSize
    }

    override fun updateRenderBounds(canvasView: CanvasView) {
        super.updateRenderBounds(canvasView)
        if (bounds.isEmpty) {
            bounds.set(0f, 0f, paint.textWidth(textItem.text), paint.textHeight())
        }
    }

    override fun render(canvas: Canvas) {
        canvas.withMatrix(canvasViewBox.matrix) {
            canvas.drawText(textItem.text ?: "", bounds.left, bounds.bottom, paint)
        }
    }

}
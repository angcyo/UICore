package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.TextItem
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.adjustSize
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import kotlin.math.max

/**
 * 文本组件渲染
 * [TextItem]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TextItemRenderer(canvasViewBox: CanvasViewBox) : BaseItemRenderer<TextItem>(canvasViewBox) {

    val paint = createPaint(Color.BLACK, Paint.Style.FILL).apply {
        //init
        textSize = 12 * dp
    }

    override fun onUpdateRendererItem(item: TextItem) {
        super.onUpdateRendererItem(item)
        val textWidth = paint.textWidth(rendererItem?.text)
        val textHeight = paint.textHeight()
        if (bounds.isEmpty) {
            bounds.set(0f, 0f, textWidth, textHeight)
        } else {
            if (textWidth > 0 && textHeight > 0) {
                bounds.adjustSize(textWidth, textHeight)
            }
        }
    }

    override fun onUpdateRendererBounds(canvasView: CanvasView) {
        super.onUpdateRendererBounds(canvasView)
        if (bounds.isEmpty) {
            bounds.set(0f, 0f, paint.textWidth(rendererItem?.text), paint.textHeight())
        }
    }

    //val _rect = Rect()

    override fun scaleBy(scaleX: Float, scaleY: Float) {
        super.scaleBy(scaleX, scaleY)
        val max = max(scaleX, scaleY)
        paint.textSize = paint.textSize * max
        bounds.adjustSize(paint.textWidth(rendererItem?.text ?: ""), paint.textHeight())

        //paint.getTextBounds(textItem.text, 0, textItem.text?.length ?: 0, _rect)//这样测量出来的文本高度, 非行高
        //bounds.adjustSize(_rect.width().toFloat(), _rect.height().toFloat())
    }

    override fun render(canvas: Canvas) {
        canvas.drawText(
            rendererItem?.text ?: "",
            bounds.left,
            bounds.bottom - paint.descent(),
            paint
        )
    }
}
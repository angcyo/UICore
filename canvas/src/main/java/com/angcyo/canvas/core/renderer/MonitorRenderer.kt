package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils.createPaint
import com.angcyo.drawable.textHeight
import com.angcyo.drawable.textWidth
import com.angcyo.library.ex.dp

/**
 * 调试监视渲染
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class MonitorRenderer(canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseRenderer(canvasViewBox, transformer), ICanvasListener {

    val paint = createPaint().apply {
        textSize = 12 * dp
        style = Paint.Style.FILL
    }

    var _isTouchDown: Boolean = false

    init {
        canvasViewBox.canvasView.canvasListenerList.add(this)
    }

    override fun updateRenderBounds(canvasView: CanvasView) {
        super.updateRenderBounds(canvasView)
    }

    override fun onCanvasTouchEvent(event: MotionEvent) {
        super.onCanvasTouchEvent(event)
        val action = event.actionMasked
        _isTouchDown = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE
        canvasViewBox.canvasView.invalidate()
    }

    override fun render(canvas: Canvas) {
        if (_isTouchDown) {
            //绘制当前的缩放比例
            val text = "${(canvasViewBox._scaleX * 100).toInt()}%"
            val x = canvasViewBox.getContentRight() - paint.textWidth(text) - 10 * dp
            val y = canvasViewBox.getContentBottom() - paint.textHeight()
            canvas.drawText(text, x, y, paint)
        }
    }
}
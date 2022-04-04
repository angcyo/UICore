package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.view.MotionEvent
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.getMaxLineWidth
import com.angcyo.canvas.utils.mapPoint
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

    val paint = createTextPaint().apply {
        //
    }

    var layout: StaticLayout? = null

    var _isTouchDown: Boolean = false

    val _touchPoint = PointF()

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
        if (_isTouchDown) {
            _touchPoint.set(event.x, event.y)
        }
        canvasViewBox.canvasView.invalidate()
    }

    override fun render(canvas: Canvas) {
        if (_isTouchDown) {
            //绘制当前的缩放比例
            val valueUnit = canvasViewBox.valueUnit
            val rect = canvasViewBox.getContentMatrixBounds()
            val touchPoint = canvasViewBox.matrix.mapPoint(_touchPoint)
            val tps = buildString {
                append("(${touchPoint.x}, ${touchPoint.y})")
                append(":")
                val xValue = valueUnit.convertPixelToValue(touchPoint.x)
                val yValue = valueUnit.convertPixelToValue(touchPoint.y)
                append(
                    "(${valueUnit.formattedValueUnit(xValue)}, ${
                        valueUnit.formattedValueUnit(yValue)
                    })"
                )
            }
            val text = "${tps}\n${rect}\n${(canvasViewBox._scaleX * 100).toInt()}%"
            assumeLayout(text)

            canvas.withTranslation(x = canvasViewBox.getContentLeft(), y = bounds.top) {
                layout?.draw(canvas)
            }
        }
    }

    var _layoutSource: CharSequence? = null
    fun assumeLayout(source: CharSequence) {
        if (layout == null || _layoutSource != source) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                layout = StaticLayout.Builder.obtain(
                    source,
                    0,
                    source.length,
                    paint,
                    canvasViewBox.getContentWidth().toInt()
                ).setAlignment(Layout.Alignment.ALIGN_OPPOSITE).build()
            } else {
                layout = StaticLayout(
                    source,
                    0,
                    source.length,
                    paint,
                    canvasViewBox.getContentWidth().toInt(),
                    Layout.Alignment.ALIGN_OPPOSITE,
                    1f,
                    0f,
                    false
                )
            }
            _layoutSource = source

            val width = layout!!.getMaxLineWidth()
            val height = layout!!.height
            val right = canvasViewBox.getContentRight() - 10 * dp
            val bottom = canvasViewBox.getContentBottom()
            bounds.set(right - width, bottom - height, right, bottom)
        }
    }
}
package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.view.MotionEvent
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils.*
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
        //init
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
        if (_isTouchDown || BuildConfig.DEBUG) {
            //绘制当前的缩放比例
            val valueUnit = canvasViewBox.valueUnit
            val _rect = canvasViewBox.contentRect
            //val rect = canvasViewBox.getContentMatrixBounds()

            canvasViewBox.matrix.invert(_tempMatrix)
            val rect = _tempMatrix.mapRectF(_rect)
            val touchPoint = _tempMatrix.mapPoint(_touchPoint)

            val tpStr = buildString {
                val xValue = valueUnit.convertPixelToValue(touchPoint.x - _rect.left)
                val yValue = valueUnit.convertPixelToValue(touchPoint.y - _rect.top)

                val xUnit = valueUnit.formattedValueUnit(xValue)
                val yUnit = valueUnit.formattedValueUnit(yValue)
                append("($xUnit, $yUnit)")
                appendLine()
                append("touch:(${_touchPoint.x}, ${_touchPoint.y})") //touch在视图上的真实坐标
                append("->")
                append("(${touchPoint.x}, ${touchPoint.y})") //映射后的坐标

                //当前视图中点, 距离坐标系左上角的距离 像素和单位数值
                val centerPoint = canvasViewBox.getContentMatrixPoint()
                val valuePoint = canvasViewBox.calcDistanceValueWithOrigin(centerPoint)

                val centerXUnit = valueUnit.formattedValueUnit(valuePoint.x)
                val centerYUnit = valueUnit.formattedValueUnit(valuePoint.y)

                appendLine()
                append("center:(${centerPoint.x}, ${centerPoint.y}):($centerXUnit, ${centerYUnit})")

                /*val _centerPoint = PointF(_rect.centerX(), _rect.centerY())
                val centerPoint = PointF(rect.centerX(), rect.centerY())
                appendLine()
                append("center:(${_centerPoint.x}, ${_centerPoint.y})") //touch在视图上的真实坐标
                append("->")
                append("(${centerPoint.x}, ${centerPoint.y})") //映射后的坐标*/
            }

            val rectStr = buildString {
                append("(${_rect.left}, ${_rect.top}, ${_rect.right}, ${_rect.bottom})")
                append("↓\n")
                append("(${rect.left}, ${rect.top}, ${rect.right}, ${rect.bottom})") //映射后
            }

            val text = "${tpStr}\n${rectStr}\n${(canvasViewBox._scaleX * 100).toInt()}%"
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
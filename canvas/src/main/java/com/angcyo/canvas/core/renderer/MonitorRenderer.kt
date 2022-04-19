package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.view.MotionEvent
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.getMaxLineWidth
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF

/**
 * 调试监视渲染
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class MonitorRenderer(canvasViewBox: CanvasViewBox) : BaseRenderer(canvasViewBox), ICanvasListener {

    val paint = createTextPaint().apply {
        //init
    }

    var layout: StaticLayout? = null

    var _isTouchDown: Boolean = false

    val _touchPoint = PointF()

    init {
        canvasViewBox.canvasView.addCanvasListener(this)
    }

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        super.onCanvasSizeChanged(canvasView)
    }

    override fun onCanvasTouchEvent(event: MotionEvent) {
        super.onCanvasTouchEvent(event)
        val action = event.actionMasked
        _isTouchDown = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE
        if (_isTouchDown) {
            _touchPoint.set(event.x, event.y)
        }
        canvasViewBox.canvasView.refresh()
    }

    override fun onCanvasBoxMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
    }

    //缓存
    val _tempMatrix: Matrix = Matrix()
    val _tempPoint: PointF = PointF()
    val _tempRect: RectF = RectF()

    override fun render(canvasView: CanvasView, canvas: Canvas) {
        if (_isTouchDown) {
            val text = if (BuildConfig.DEBUG) {

                //绘制当前的缩放比例
                val valueUnit = canvasViewBox.valueUnit
                val _rect = canvasViewBox.contentRect
                //val rect = canvasViewBox.getContentMatrixBounds()

                canvasViewBox.matrix.invert(_tempMatrix)
                val rect = _tempMatrix.mapRectF(_rect)
                val touchPoint = _tempMatrix.mapPoint(_touchPoint, _tempPoint)

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
                    val centerPoint = canvasViewBox.getCoordinateSystemCenter()

                    val centerXUnit = valueUnit.convertPixelToValueUnit(centerPoint.x)
                    val centerYUnit = valueUnit.convertPixelToValueUnit(centerPoint.y)

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

                "${tpStr}\n${rectStr}\n${(canvasViewBox._scaleX * 100).toInt()}%"
            } else {
                "${(canvasViewBox._scaleX * 100).toInt()}%"
            }

            //StaticLayout
            ensureLayout(text)

            //draw
            canvas.withTranslation(x = canvasViewBox.getContentLeft(), y = _renderBounds.top) {
                layout?.draw(canvas)
            }
        }
    }

    var _layoutSource: CharSequence? = null
    fun ensureLayout(source: CharSequence) {
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
            val right = canvasViewBox.getContentRight()
            val bottom = canvasViewBox.getContentBottom()
            _renderBounds.set(right - width, bottom - height, right, bottom)
        }
    }
}
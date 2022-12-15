package com.angcyo.canvas.core.renderer

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.toColor
import com.angcyo.library.unit.IValueUnit

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxisRenderer(canvasView: ICanvasView) : BaseRenderer(canvasView) {

    /**主要线的画笔*/
    val lineProtrudePaint = createPaint(Color.GRAY)

    /**绘制刻度的画笔*/
    val linePaint = createPaint("#d0d0d0".toColor())

    /**主要网格线的画笔*/
    val gridProtrudePaint = createPaint(Color.GRAY)

    /**普通网格线的画笔*/
    val gridPaint = createPaint("#d0d0d0".toColor())

    /**label的文本大小*/
    var labelTextSize = 9 * dp

    /**绘制刻度文字的画笔*/
    val labelPaint = createPaint(Color.GRAY).apply {
        textSize = labelTextSize
        style = Paint.Style.FILL
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    ) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        updateAxisData()
    }

    /**坐标系原点改变后回调*/
    fun onCoordinateSystemOriginChanged(point: PointF) {
        updateAxisData()
    }

    /**坐标系单位改变后回调*/
    fun onCoordinateSystemUnitChanged(valueUnit: IValueUnit) {
        updateAxisData()
    }

    /**更新坐标数据*/
    abstract fun updateAxisData()

    /**自动计算文本画笔的大小*/
    fun calcLabelPaintSize(axis: BaseAxis, label: String) {
        labelPaint.textSize = labelTextSize
        if (axis is YAxis) {
            val requestWidth = labelPaint.textWidth(label) + axis.labelXOffset * 2
            val renderWidth = getRenderBounds().width()
            if (requestWidth > renderWidth) {
                labelPaint.textSize = labelPaint.textSize * renderWidth / requestWidth
            }
        }
    }

}
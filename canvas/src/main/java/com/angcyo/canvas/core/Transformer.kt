package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF

/**
 * 变压器, 用来将像素坐标转换成绘图可视化坐标
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class Transformer(val canvasViewBox: CanvasViewBox) {

    /**额外的矩阵转换器*/
    val transformerMatrix: Matrix = Matrix()

    /**将坐标矩形, 映射成变换后的矩形*/
    fun mapRectF(rect: RectF, result: RectF): RectF {
        return transformerMatrix.mapRectF(rect, result)
    }

    /**将坐标矩形, 映射成变换后的矩形*/
    fun mapPointF(point: PointF, result: PointF): PointF {
        return transformerMatrix.mapPoint(point, result)
    }

}
package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.RectF
import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.library.ex.ceilReverse

/**
 * CanvasView 内容可视区域范围
 * 内容可视区域的一些数据, 比如偏移数据, 缩放数据等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class CanvasViewBox(val view: CanvasView) {

    /**内容可视区域*/
    val contentRect = RectF()

    /**触摸带来的视图矩阵变化*/
    val matrix: Matrix = Matrix()

    /**内容区域左边额外的偏移*/
    var contentOffsetLeft = 0f
    var contentOffsetRight = 0f
    var contentOffsetTop = 0f
    var contentOffsetBottom = 0f

    /**[CanvasView]视图的宽高*/
    var canvasViewWidth: Int = 0
    var canvasViewHeight: Int = 0

    //<editor-fold desc="operate">

    /**更新可是内容范围*/
    fun updateContentBox() {
        canvasViewWidth = view.measuredWidth
        canvasViewHeight = view.measuredHeight

        contentRect.set(getContentLeft(), getContentTop(), getContentRight(), getContentBottom())

        view.invalidate()
    }

    /**刷新*/
    fun refresh(newMatrix: Matrix) {
        matrix.set(newMatrix)
        view.invalidate()
    }

    //</editor-fold desc="operate">

    //<editor-fold desc="base">

    fun getContentLeft(): Float {
        if (view.yAxisRender.yAxis.enable) {
            return view.yAxisRender.getRenderBounds().right + contentOffsetLeft
        }
        return contentOffsetLeft
    }

    fun getContentRight(): Float {
        return canvasViewWidth - contentOffsetRight
    }

    fun getContentTop(): Float {
        if (view.xAxisRender.xAxis.enable) {
            return view.xAxisRender.getRenderBounds().bottom + contentOffsetTop
        }
        return contentOffsetTop
    }

    fun getContentBottom(): Float {
        return canvasViewHeight - contentOffsetBottom
    }

    fun getContentCenterX(): Float {
        return (getContentLeft() + getContentRight()) / 2
    }

    fun getContentCenterY(): Float {
        return (getContentTop() + getContentBottom()) / 2
    }

    /**获取可视区偏移后的坐标矩形*/
    fun getContentMatrixBounds(matrix: Matrix = this.matrix): RectF {
        matrix.mapRect(_tempRectF, contentRect)
        return _tempRectF
    }

    //</editor-fold desc="base">

    //<editor-fold desc="value unit">

    /**绘制时使用的值类型, 最后要都要转换成像素, 在界面上绘制*/
    var valueType: Int = TypedValue.COMPLEX_UNIT_MM

    /**将value转换成绘制的文本*/
    var formattedValue: (value: Float) -> String = { value ->
        if (valueType == TypedValue.COMPLEX_UNIT_MM) {
            "${(value.ceilReverse() / 10).toInt()}" //mm 转换成 cm
        } else {
            "$value"
        }
    }

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    fun convertValueToPixel(value: Float): Float {
        val dm: DisplayMetrics = view.resources.displayMetrics
        return TypedValue.applyDimension(valueType, value, dm)
    }

    /**将像素转换为单位数值*/
    fun convertPixelToValue(pixel: Float): Float {
        val unit = convertValueToPixel(1f)
        return pixel / unit
    }

    //</editor-fold desc="value unit">

    //<editor-fold desc="matrix">

    /**平移视图
     * 正向移动后, 绘制的内容在正向指定的位置绘制*/
    fun translateBy(distanceX: Float, distanceY: Float) {
        val newMatrix = Matrix()
        newMatrix.set(matrix)
        newMatrix.postTranslate(distanceX, distanceY)
        refresh(newMatrix)
    }

    /**缩放视图*/
    fun scaleBy(
        scaleX: Float,
        scaleY: Float,
        px: Float = getContentCenterX(),
        py: Float = getContentCenterY()
    ) {
        val newMatrix = Matrix()
        newMatrix.set(matrix)
        newMatrix.postScale(scaleX, scaleY, px, py)
        refresh(newMatrix)
    }

    //</editor-fold desc="matrix">


}
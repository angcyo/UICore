package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.RectF
import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.utils._tempMatrix
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.canvas.utils._tempValues
import com.angcyo.canvas.utils.clamp
import com.angcyo.library.ex.ceilReverse

/**
 * CanvasView 内容可视区域范围
 * 内容可视区域的一些数据, 比如偏移数据, 缩放数据等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class CanvasViewBox(val canvasView: CanvasView) {

    //<editor-fold desc="控制属性">

    /**触摸带来的视图矩阵变化*/
    val matrix: Matrix = Matrix()

    /**内容区域左边额外的偏移*/
    var contentOffsetLeft = 0f
    var contentOffsetRight = 0f
    var contentOffsetTop = 0f
    var contentOffsetBottom = 0f

    /**最小和最大的缩放比例*/
    var minScaleX: Float = 0.25f
    var maxScaleX: Float = 5f
    var minScaleY: Float = 0.25f
    var maxScaleY: Float = 5f

    /**最小和最大的平移距离*/
    var minTranslateX: Float = -Float.MAX_VALUE
    var maxTranslateX: Float = 0f
    var minTranslateY: Float = -Float.MAX_VALUE
    var maxTranslateY: Float = 0f

    //</editor-fold desc="控制属性">

    //<editor-fold desc="存储属性">

    /**内容可视区域*/
    val _contentRect = RectF()

    /**[CanvasView]视图的宽高*/
    var _canvasViewWidth: Int = 0
    var _canvasViewHeight: Int = 0

    //当前的缩放比例
    var _scaleX: Float = 1f
    var _scaleY: Float = 1f

    //当前平移的距离, 像素
    var _translateX: Float = 0f
    var _translateY: Float = 0f

    //</editor-fold desc="存储属性">

    //<editor-fold desc="operate">

    /**更新可是内容范围*/
    fun updateContentBox() {
        _canvasViewWidth = canvasView.measuredWidth
        _canvasViewHeight = canvasView.measuredHeight

        _contentRect.set(getContentLeft(), getContentTop(), getContentRight(), getContentBottom())

        //刷新视图
        refresh(matrix)
    }

    /**刷新*/
    fun refresh(newMatrix: Matrix) {
        val oldMatrix = Matrix(matrix)
        canvasView.canvasListenerList.forEach {
            it.onCanvasMatrixChangeBefore(matrix)
        }
        _tempMatrix.set(newMatrix)
        matrix.set(newMatrix)
        _tempMatrix.set(newMatrix)
        limitTranslateAndScale(matrix)
        canvasView.invalidate()
        canvasView.canvasListenerList.forEach {
            it.onCanvasMatrixChangeAfter(matrix, oldMatrix)
        }
    }

    //</editor-fold desc="operate">

    //<editor-fold desc="base">

    fun getContentLeft(): Float {
        if (canvasView.yAxisRender.axis.enable) {
            return canvasView.yAxisRender.getRenderBounds().right + contentOffsetLeft
        }
        return contentOffsetLeft
    }

    fun getContentRight(): Float {
        return _canvasViewWidth - contentOffsetRight
    }

    fun getContentTop(): Float {
        if (canvasView.xAxisRender.axis.enable) {
            return canvasView.xAxisRender.getRenderBounds().bottom + contentOffsetTop
        }
        return contentOffsetTop
    }

    fun getContentBottom(): Float {
        return _canvasViewHeight - contentOffsetBottom
    }

    fun getContentCenterX(): Float {
        return (getContentLeft() + getContentRight()) / 2
    }

    fun getContentCenterY(): Float {
        return (getContentTop() + getContentBottom()) / 2
    }

    /**获取可视区偏移后的坐标矩形*/
    fun getContentMatrixBounds(matrix: Matrix = this.matrix): RectF {
        matrix.mapRect(_tempRectF, _contentRect)
        return _tempRectF
    }

    /**限制[matrix]可视化窗口的平移和缩放大小
     * [matrix] 输入和输出的 对象*/
    fun limitTranslateAndScale(matrix: Matrix) {
        matrix.getValues(_tempValues)

        val curScaleX: Float = _tempValues[Matrix.MSCALE_X]
        val curScaleY: Float = _tempValues[Matrix.MSCALE_Y]

        val curTransX: Float = _tempValues[Matrix.MTRANS_X]
        val curTransY: Float = _tempValues[Matrix.MTRANS_Y]

        _scaleX = clamp(curScaleX, minScaleX, maxScaleX)
        _scaleY = clamp(curScaleY, minScaleY, maxScaleY)

        _translateX = clamp(curTransX, minTranslateX, maxTranslateX)
        _translateY = clamp(curTransY, minTranslateY, maxTranslateY)

        _tempValues[Matrix.MTRANS_X] = _translateX
        _tempValues[Matrix.MTRANS_Y] = _translateY

        _tempValues[Matrix.MSCALE_X] = _scaleX
        _tempValues[Matrix.MSCALE_Y] = _scaleY

        matrix.setValues(_tempValues)
    }

    /**
     * 调整缩放到限制范围
     * 返回缩放是否超出了限制*/
    fun adjustScaleOutToLimit(matrix: Matrix): Boolean {
        matrix.getValues(_tempValues)

        val curScaleX: Float = _tempValues[Matrix.MSCALE_X]
        val curScaleY: Float = _tempValues[Matrix.MSCALE_Y]

        var adjustX = 1f
        var adjustY = 1f

        if (curScaleX <= minScaleX) {
            adjustX = minScaleX / curScaleX
        } else if (curScaleX >= maxScaleX) {
            adjustX = maxScaleX / curScaleX
        }

        if (curScaleY <= minScaleY) {
            adjustY = minScaleY / curScaleY
        } else if (curScaleY >= maxScaleY) {
            adjustY = maxScaleY / curScaleY
        }

        if (adjustX != 1f || adjustY != 1f) {
            matrix.postScale(adjustX, adjustY)
            return true
        }

        return false
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
        val dm: DisplayMetrics = canvasView.resources.displayMetrics
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
        if ((scaleX < 1f && _scaleX <= minScaleX) || (scaleX > 1f && _scaleX >= maxScaleX)) {
            //已经达到了最小/最大, 还想缩放/放大
            return
        }

        if ((scaleY < 1f && _scaleY <= minScaleY) || (scaleY > 1f && _scaleY >= maxScaleY)) {
            return
        }

        val newMatrix = Matrix()
        newMatrix.set(matrix)
        newMatrix.postScale(scaleX, scaleY, px, py)
        adjustScaleOutToLimit(newMatrix)
        refresh(newMatrix)
    }

    //</editor-fold desc="matrix">
}
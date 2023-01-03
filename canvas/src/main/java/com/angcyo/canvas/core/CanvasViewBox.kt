package com.angcyo.canvas.core

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.UiThread
import com.angcyo.library.L
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.ex.*
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.MmValueUnit

/**
 * CanvasView 内容可视区域范围
 * 内容可视区域的一些数据, 比如偏移数据, 缩放数据等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class CanvasViewBox(val canvasView: ICanvasView) {

    //<editor-fold desc="控制属性">

    /**内容区域左边额外的偏移*/
    var contentOffsetLeft = 0f
    var contentOffsetRight = 0f
    var contentOffsetTop = 0f
    var contentOffsetBottom = 0f

    /**最小和最大的缩放比例*/
    var minScaleX: Float = 0.05f
    var maxScaleX: Float = 10f //5f

    var minScaleY: Float = minScaleX
    var maxScaleY: Float = 10f //5f

    /**最小和最大的平移距离*/
    var minTranslateX: Float = -Float.MAX_VALUE
    var maxTranslateX: Float = Float.MAX_VALUE //0f

    var minTranslateY: Float = -Float.MAX_VALUE
    var maxTranslateY: Float = Float.MAX_VALUE //0f

    //</editor-fold desc="控制属性">

    //<editor-fold desc="存储属性">

    /**触摸带来的视图矩阵变化*/
    val matrix: Matrix = Matrix()

    //刷新之前的矩阵
    var oldMatrix: Matrix? = null

    /**[matrix]的逆矩阵
     * [com.angcyo.canvas.core.CanvasViewBox.refresh]
     * */
    val invertMatrix: Matrix = Matrix()

    /**内容可视区域*/
    val contentRect = emptyRectF()

    val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val _tempPoint = PointF()
    val _tempRect: RectF = emptyRectF()

    //</editor-fold desc="存储属性">

    //<editor-fold desc="operate">

    fun updateContentBox(rect: RectF) {
        updateContentBox(rect.left, rect.top, rect.right, rect.bottom)
    }

    /**更新可是内容范围*/
    fun updateContentBox(left: Float, top: Float, right: Float, bottom: Float) {
        contentRect.set(
            left + contentOffsetLeft,
            top + contentOffsetTop,
            right - contentOffsetRight,
            bottom - contentOffsetBottom
        )
        //刷新视图
        refresh(matrix)
    }

    /**刷新*/
    @UiThread
    fun refresh(newMatrix: Matrix, isEnd: Boolean = true) {
        if (oldMatrix == null) {
            oldMatrix = Matrix(matrix)
        }
        canvasView.dispatchCanvasBoxMatrixChangeBefore(matrix, newMatrix)

        matrix.set(newMatrix)
        limitTranslateAndScale(matrix)

        //反转矩阵后的值
        matrix.invert(invertMatrix)

        canvasView.dispatchCanvasBoxMatrixChanged(matrix, oldMatrix!!, isEnd)
        if (isEnd) {
            oldMatrix = null
        }

        //刷新view
        canvasView.refresh()
    }

    /**在当前视图中心获取一个坐标系中指定宽高的矩形*/
    fun getCoordinateSystemCenterRect(
        width: Float,
        height: Float,
        result: RectF = _tempRect
    ): RectF {
        val point = getCoordinateSystemCenter()
        result.set(
            point.x - width / 2,
            point.y - height / 2,
            point.x + width / 2,
            point.y + height / 2
        )
        return result
    }

    /**计算任意一点, 与坐标系原点的距离, 返回的是[ValueUnit]对应的值
     * [point] 转换后的点像素坐标, [View]左上角坐标系坐标*/
    fun calcDistanceValueWithOrigin(point: PointF, result: PointF = _tempPoint): PointF {
        val pixelPoint = calcDistancePixelWithOrigin(point)

        val xValue = valueUnit.convertPixelToValue(pixelPoint.x.toDouble())
        val yValue = valueUnit.convertPixelToValue(pixelPoint.y.toDouble())

        result.set(xValue.toFloat(), yValue.toFloat())
        return result
    }

    /**计算任意一点, 与坐标系原点的距离
     * [point] 转换后的点像素坐标*/
    fun calcDistancePixelWithOrigin(point: PointF, result: PointF = _tempPoint): PointF {
        //val originPoint = getCoordinateSystemPoint()
        val xPixelValue = point.x - getCoordinateSystemX()
        val yPixelValue = point.y - getCoordinateSystemY()

        result.set(xPixelValue, yPixelValue)
        return result
    }

    /**[bounds] 相对于坐标系原点的坐标
     * [result] 返回相对于视图原点的坐标*/
    fun calcItemRenderBounds(bounds: RectF, result: RectF): RectF {
        _tempRect.set(bounds)
        _tempRect.offset(getCoordinateSystemX(), getCoordinateSystemY())
        result.set(_tempRect)
        return result
    }

    //---

    /**获取当前能够看到的坐标系的范围矩形, 肉眼坐标保持不变.
     * [contentRect] 保持原先的位置, [matrix]缩放平移之后,一个新的[RectF]
     * */
    fun getVisualRect(result: RectF = acquireTempRectF()): RectF {
        invertMatrix.mapRect(result, contentRect)
        return result
    }

    /**将可视化矩形, 映射成坐标系矩形
     * 可视化的坐标, 映射成坐标系中的坐标.
     * 比如: 当前手势按下在View的[100,100]处,此时返回在坐标系中的[x,y]处*/
    fun mapCoordinateSystemRect(rect: RectF, result: RectF = _tempRect): RectF {
        invertMatrix.mapRect(result, rect)
        return result
    }

    /**将可视化坐标点, 映射成坐标系点
     * [viewPointToCoordinateSystemPoint]*/
    fun mapCoordinateSystemPoint(
        point: PointF,
        result: PointF = _tempPoint,
        operateMatrix: Matrix = invertMatrix
    ): PointF {
        return operateMatrix.mapPoint(point, result)
    }

    /**将屏幕上的点坐标, 映射成坐标系中的坐标. 没有偏移到坐标系原点*/
    fun mapCoordinateSystemPoint(
        x: Float,
        y: Float,
        result: PointF = _tempPoint,
        operateMatrix: Matrix = invertMatrix
    ): PointF {
        _tempPoint.set(x, y)
        return operateMatrix.mapPoint(_tempPoint, result)
    }

    /**将视图坐标点[point] 转换成对应的坐标系中的点[result]
     * [coordinateSystemPointToViewPoint]*/
    fun viewPointToCoordinateSystemPoint(
        point: PointF,
        result: PointF = _tempPoint,
        operateMatrix: Matrix = invertMatrix
    ): PointF {
        operateMatrix.mapPoint(point, result)
        result.offset(-getCoordinateSystemX(), -getCoordinateSystemY())
        return result
    }

    //---

    /**将坐标系中的坐标, 转换成view上的坐标
     * [viewPointToCoordinateSystemPoint]*/
    fun coordinateSystemPointToViewPoint(
        point: PointF,
        result: PointF = _tempPoint,
        operateMatrix: Matrix = matrix
    ): PointF {
        result.set(point.x, point.y)
        result.offset(getCoordinateSystemX(), getCoordinateSystemY())
        operateMatrix.mapPoint(result, result)
        return result
    }

    /**将坐标系中的矩形, 转换成可以直接绘制的矩形*/
    fun coordinateSystemRectToViewRect(rect: RectF, result: RectF = _tempRect): RectF {
        result.set(rect)
        result.offset(getCoordinateSystemX(), getCoordinateSystemY())
        calcItemVisualBounds(result, result)
        return result
    }

    /**获取当前坐标系在可视窗口的矩形, 原始坐标保持不变.
     * [contentRect] 缩放平移[matrix]之后的变成的[RectF]
     * */
    fun getVisualCoordinateSystemRect(result: RectF = acquireTempRectF()): RectF {
        /*matrix.getValues(_tempValues)
        _tempValues[Matrix.MTRANS_X] -= _tempValues[Matrix.MTRANS_X]
        _tempValues[Matrix.MTRANS_Y] -= _tempValues[Matrix.MTRANS_Y]
        tempMatrix.setValues(_tempValues)
        tempMatrix.mapRect(_tempRectF, contentRect)*/
        matrix.mapRect(result, contentRect)
        return result
    }

    /**计算[item]在当前视图中的坐标, 相对于[view]左上角的矩形坐标
     * [bounds] 可以直接绘制的坐标*/
    fun calcItemVisualBounds(bounds: RectF, result: RectF): RectF {
        //重点

        //映射之后, 坐标相对于视图左上角的坐标
        matrix.mapRectF(bounds, result)

        //将相对于与视图左上角的坐标转换成可以直接绘制的坐标, 最终会和bounds一直
        /*val test = emptyRectF()
        invertMatrix.mapRectF(result, test)*/

        return result
    }

    //</editor-fold desc="operate">

    //<editor-fold desc="base">

    fun isCanvasInit(): Boolean = !contentRect.isEmpty

    fun getContentLeft(): Float = contentRect.left

    fun getContentRight(): Float = contentRect.right

    fun getContentTop(): Float = contentRect.top

    fun getContentBottom(): Float = contentRect.bottom

    fun getContentCenterX(): Float {
        return (getContentLeft() + getContentRight()) / 2
    }

    fun getContentCenterY(): Float {
        return (getContentTop() + getContentBottom()) / 2
    }

    fun getContentWidth() = getContentRight() - getContentLeft()

    fun getContentHeight() = getContentBottom() - getContentTop()

    fun getScaleX(): Float {
        return matrix.getScaleX()
    }

    fun getScaleY(): Float {
        return matrix.getScaleY()
    }

    fun getTranslateX(): Float {
        return matrix.getTranslateX()
    }

    fun getTranslateY(): Float {
        return matrix.getTranslateY()
    }

    /**限制[matrix]可视化窗口的平移和缩放大小
     * [matrix] 输入和输出的 对象*/
    fun limitTranslateAndScale(matrix: Matrix) {
        matrix.getValues(_tempValues)

        val curScaleX: Float = _tempValues[Matrix.MSCALE_X]
        val curScaleY: Float = _tempValues[Matrix.MSCALE_Y]

        val curTransX: Float = _tempValues[Matrix.MTRANS_X]
        val curTransY: Float = _tempValues[Matrix.MTRANS_Y]

        val scaleX = clamp(curScaleX, minScaleX, maxScaleX)
        val scaleY = clamp(curScaleY, minScaleY, maxScaleY)

        val translateX = clamp(curTransX, minTranslateX, maxTranslateX)
        val translateY = clamp(curTransY, minTranslateY, maxTranslateY)

        _tempValues[Matrix.MTRANS_X] = translateX
        _tempValues[Matrix.MTRANS_Y] = translateY

        _tempValues[Matrix.MSCALE_X] = scaleX
        _tempValues[Matrix.MSCALE_Y] = scaleY

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

    /**像素, 坐标单位转换*/
    var valueUnit: IValueUnit = MmValueUnit()

    /**更新坐标单位*/
    fun updateCoordinateSystemUnit(valueUnit: IValueUnit) {
        val old = valueUnit
        this.valueUnit = valueUnit
        canvasView.dispatchCoordinateSystemUnitChanged(old, valueUnit)
        canvasView.refresh()
    }

    //</editor-fold desc="value unit">

    //<editor-fold desc="coordinate system">

    /**坐标系的原点像素坐标*/
    val coordinateSystemOriginPoint: PointF = PointF(0f, 0f)

    /**更新坐标系原点*/
    fun updateCoordinateSystemOriginPoint(x: Float, y: Float) {
        if (coordinateSystemOriginPoint.x != x || coordinateSystemOriginPoint.y != y) {
            coordinateSystemOriginPoint.set(x, y)
            canvasView.dispatchCoordinateSystemOriginChanged(coordinateSystemOriginPoint)
            canvasView.refresh()
        }
    }

    /**获取当前视图中心距离坐标系原点的坐标*/
    fun getCoordinateSystemCenter(result: PointF = _tempPoint): PointF {
        //转换后中点对应的像素坐标
        val contentCenterX = getContentCenterX()
        val contentCenterY = getContentCenterY()
        val centerPoint = invertMatrix.mapPoint(contentCenterX, contentCenterY)
        centerPoint.x -= getCoordinateSystemX()
        centerPoint.y -= getCoordinateSystemY()
        return result.apply { set(centerPoint) }
    }

    /**获取坐标系原点的x坐标*/
    fun getCoordinateSystemX(): Float {
        return coordinateSystemOriginPoint.x
    }

    /**获取坐标系原点的y坐标*/
    fun getCoordinateSystemY(): Float {
        return coordinateSystemOriginPoint.y
    }

    /**获取系统坐标系转换后的所在的像素坐标位置*/
    fun getCoordinateSystemPoint(result: PointF = _tempPoint): PointF {
        result.set(invertMatrix.mapPoint(getCoordinateSystemX(), getCoordinateSystemY()))
        return result
    }

    //</editor-fold desc="coordinate system">

    //<editor-fold desc="matrix">

    var _updateAnimator: ValueAnimator? = null

    /**重置坐标系*/
    fun updateTo(
        endMatrix: Matrix = Matrix(),
        anim: Boolean = true,
        finish: (isCancel: Boolean) -> Unit = {}
    ) {
        _updateAnimator?.cancel()
        _updateAnimator = null
        if (anim) {
            _updateAnimator = matrixAnimator(matrix, endMatrix, finish = { isCancel ->
                _updateAnimator = null
                if (!isCancel) {
                    refresh(endMatrix)
                }
                finish(isCancel)//
            }) {
                adjustScaleOutToLimit(it)
                refresh(it, false)
            }
        } else {
            adjustScaleOutToLimit(endMatrix)
            refresh(endMatrix)
            finish(false)//
        }
    }

    fun updateToMatrix(anim: Boolean = true, endMatrix: Matrix.() -> Unit) {
        updateTo(Matrix().apply(endMatrix), anim)
    }

    fun reset(anim: Boolean = true) {
        val newMatrix = Matrix()
        updateTo(newMatrix, anim)
    }

    /**平移视图
     * 正向移动后, 绘制的内容在正向指定的位置绘制
     * */
    fun translateBy(distanceX: Float, distanceY: Float, anim: Boolean = true) {
        L.d("平移画布:dx:${distanceX} dy:${distanceY} $anim")
        val newMatrix = Matrix()
        newMatrix.set(matrix)
        newMatrix.postTranslate(distanceX, distanceY)
        updateTo(newMatrix, anim)
    }

    /**直接平移到*/
    fun translateTo(x: Float, y: Float, anim: Boolean = true) {
        val newMatrix = Matrix()
        newMatrix.setTranslate(x, y)
        newMatrix.postScale(
            getScaleX(),
            getScaleY(),
            getContentLeft() + x,
            getContentTop() + y
        )
        updateTo(newMatrix, anim)
    }

    /**直接缩放视图到指定比例*/
    fun scaleTo(
        scaleX: Float,
        scaleY: Float,
        px: Float = getContentCenterX(),
        py: Float = getContentCenterY(),
        anim: Boolean = true
    ) {
        val sx = clamp(scaleX, minScaleX, maxScaleX)
        val sy = clamp(scaleY, minScaleY, maxScaleY)

        val newMatrix = Matrix()
        newMatrix.setTranslate(matrix.getTranslateX(), matrix.getTranslateY())
        newMatrix.postScale(sx, sy, px, py)
        updateTo(newMatrix, anim)
    }

    /**缩放视图*/
    fun scaleBy(
        scaleX: Float,
        scaleY: Float,
        px: Float = getContentCenterX(),
        py: Float = getContentCenterY(),
        anim: Boolean = false,
        finish: (isCancel: Boolean) -> Unit = {}
    ) {
        if ((scaleX < 1f && getScaleX() <= minScaleX) || (scaleX > 1f && getScaleX() >= maxScaleX)) {
            //已经达到了最小/最大, 还想缩放/放大
            return
        }

        if ((scaleY < 1f && getScaleY() <= minScaleY) || (scaleY > 1f && getScaleY() >= maxScaleY)) {
            return
        }
        L.d("缩放画布:sx:${scaleX} sy:${scaleY} x:${px} y:${py} $anim")

        val newMatrix = Matrix()
        newMatrix.set(matrix)
        newMatrix.postScale(scaleX, scaleY, px, py)

        updateTo(newMatrix, anim, finish)
    }

    //</editor-fold desc="matrix">
}
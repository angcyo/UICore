package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.renderer.items.IItemRenderer
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.canvas.utils.clamp
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF

/**
 * CanvasView 内容可视区域范围
 * 内容可视区域的一些数据, 比如偏移数据, 缩放数据等
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class CanvasViewBox(val canvasView: CanvasView) {

    //<editor-fold desc="控制属性">

    /**内容区域左边额外的偏移*/
    var contentOffsetLeft = 0f
    var contentOffsetRight = 0f
    var contentOffsetTop = 0f
    var contentOffsetBottom = 0f

    /**最小和最大的缩放比例*/
    var minScaleX: Float = 0.25f
    var maxScaleX: Float = 10f //5f

    var minScaleY: Float = 0.25f
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
    val oldMatrix: Matrix = Matrix()

    //临时变量
    val tempMatrix: Matrix = Matrix()
    val invertMatrix: Matrix = Matrix()

    /**内容可视区域*/
    val contentRect = RectF()

    /**[CanvasView]视图的宽高*/
    var _canvasViewWidth: Int = 0
    var _canvasViewHeight: Int = 0

    //当前的缩放比例
    var _scaleX: Float = 1f
    var _scaleY: Float = 1f

    //当前平移的距离, 像素
    var _translateX: Float = 0f
    var _translateY: Float = 0f

    val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val _tempPoint = PointF()
    val _tempRect: RectF = RectF()

    //</editor-fold desc="存储属性">

    //<editor-fold desc="operate">

    /**更新可是内容范围*/
    fun updateContentBox() {
        _canvasViewWidth = canvasView.measuredWidth
        _canvasViewHeight = canvasView.measuredHeight

        contentRect.set(getContentLeft(), getContentTop(), getContentRight(), getContentBottom())

        //刷新视图
        refresh(matrix)
    }

    /**刷新*/
    fun refresh(newMatrix: Matrix) {
        oldMatrix.set(matrix)
        canvasView.canvasListenerList.forEach {
            it.onCanvasMatrixChangeBefore(matrix, newMatrix)
        }
        matrix.set(newMatrix)
        limitTranslateAndScale(matrix)

        canvasView.onCanvasMatrixUpdate(matrix, oldMatrix)
        canvasView.invalidate()
        canvasView.canvasListenerList.forEach {
            it.onCanvasMatrixChangeAfter(matrix, oldMatrix)
        }
    }

    /**获取变换后, 可视化的中点坐标, 像素.
     * 并非与坐标系中点的距离*/
    fun getContentMatrixPoint(result: PointF = _tempPoint): PointF {
        matrix.invert(invertMatrix)
        //转换后中点对应的像素坐标
        val contentCenterX = getContentCenterX()
        val contentCenterY = getContentCenterY()
        val centerPoint = invertMatrix.mapPoint(contentCenterX, contentCenterY)
        /*centerPoint.x -= getCoordinateSystemX()
        centerPoint.y -= getCoordinateSystemY()*/
        return result.apply { set(centerPoint) }
    }

    /**在转换后的视图中心取一个指定宽高的矩形坐标*/
    fun getContentMatrixRect(width: Float, height: Float, result: RectF = _tempRect): RectF {
        val point = getContentMatrixPoint()
        result.set(
            point.x - width / 2,
            point.y - height / 2,
            point.x + width / 2,
            point.y + height / 2
        )
        return result
    }

    /**计算任意一点, 与坐标系原点的距离, 返回的是[ValueUnit]对应的值
     * [point] 转换后的点像素坐标*/
    fun calcDistanceValueWithOrigin(point: PointF, result: PointF = _tempPoint): PointF {
        val pixelPoint = calcDistancePixelWithOrigin(point)

        val xValue = valueUnit.convertPixelToValue(pixelPoint.x)
        val yValue = valueUnit.convertPixelToValue(pixelPoint.y)

        result.set(xValue, yValue)
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

    /**将可视化坐标点, 映射成坐标系点*/
    fun mapCoordinateSystemPoint(point: PointF, result: PointF = _tempPoint): PointF {
        matrix.invert(invertMatrix)
        return invertMatrix.mapPoint(point, result)
    }

    /**将可视化矩形, 映射成坐标系矩形*/
    fun mapCoordinateSystemRect(rect: RectF, result: RectF = _tempRect): RectF {
        matrix.invert(invertMatrix)
        invertMatrix.mapRect(result, rect)
        return result
    }

    /**计算[item]在当前视图中的坐标, 相对于[view]左上角的矩形坐标*/
    fun calcItemVisibleBounds(item: IRenderer, result: RectF): RectF {
        //重点

        //bounds, 可以直接绘制的坐标
        val bounds = item.getRendererBounds()
        //映射之后, 坐标相对于视图左上角的坐标
        matrix.mapRectF(bounds, result)

        //将相对于与视图左上角的坐标转换成可以直接绘制的坐标, 最终会和bounds一直
        /*val test = RectF()
        matrix.invert(invertMatrix)
        invertMatrix.mapRectF(result, test)*/

        return result
    }

    /**计算[item]在当前坐标系中的矩形坐标*/
    fun mapItemCoordinateSystemBounds(item: IItemRenderer<*>, result: RectF): RectF {
        return result
    }

    //</editor-fold desc="operate">

    //<editor-fold desc="base">

    fun isCanvasInit(): Boolean = _canvasViewWidth > 0 && _canvasViewHeight > 0

    fun getContentLeft(): Float {
        if (canvasView.yAxisRender.axis.enable) {
            return canvasView.yAxisRender.getRendererBounds().right + contentOffsetLeft
        }
        return contentOffsetLeft
    }

    fun getContentRight(): Float {
        return _canvasViewWidth - contentOffsetRight
    }

    fun getContentTop(): Float {
        if (canvasView.xAxisRender.axis.enable) {
            return canvasView.xAxisRender.getRendererBounds().bottom + contentOffsetTop
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

    fun getContentWidth() = getContentRight() - getContentLeft()

    fun getContentHeight() = getContentBottom() - getContentTop()

    /**获取可视区偏移后的坐标矩形*/
    fun getContentMatrixBounds(matrix: Matrix = this.matrix): RectF {

        /*matrix.getValues(_tempValues)

        _tempValues[Matrix.MTRANS_X] -= _tempValues[Matrix.MTRANS_X]
        _tempValues[Matrix.MTRANS_Y] -= _tempValues[Matrix.MTRANS_Y]

        tempMatrix.setValues(_tempValues)
        tempMatrix.mapRect(_tempRectF, contentRect)*/

        matrix.mapRect(_tempRectF, contentRect)

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

    var valueUnit: ValueUnit = ValueUnit()

    //</editor-fold desc="value unit">

    //<editor-fold desc="coordinate system">

    /**获取坐标系启动的x坐标*/
    fun getCoordinateSystemX(): Float {
        return getContentLeft()
    }

    /**获取坐标系启动的y坐标*/
    fun getCoordinateSystemY(): Float {
        return getContentTop()
    }

    /**获取系统坐标系转换后的所在的像素坐标位置*/
    fun getCoordinateSystemPoint(result: PointF = _tempPoint): PointF {
        matrix.invert(invertMatrix)
        result.set(invertMatrix.mapPoint(getCoordinateSystemX(), getCoordinateSystemY()))
        return result
    }

    //</editor-fold desc="coordinate system">

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
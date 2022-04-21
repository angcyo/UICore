package com.angcyo.canvas.items.renderer

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.utils._tempPoint
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.L
import com.angcyo.library.ex.adjustSizeWithLT
import com.angcyo.library.ex.contains

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer<T : BaseItem>(canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox), IItemRenderer<T> {

    //<editor-fold desc="属性">

    /**当前旋转的角度, 在[CanvasView]中处理此属性
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    var rotate: Float = 0f

    /**当前宽度的缩放比例*/
    var scaleX: Float = 1f

    /**当前高度的缩放比例*/
    var scaleY: Float = 1f

    /**是否锁定了缩放比例*/
    var isLockScaleRatio: Boolean = true

    /**需要渲染的数据*/
    var rendererItem: T? = null
        set(value) {
            val old = field
            field = value
            if (old != value) {
                updateRendererItem(value, old)
            }
        }

    val _rotateBounds = RectF()

    /**[_rotateRenderBounds]旋转后的坐标*/
    val _rotateRenderBounds = RectF()

    /**[_visualBounds]旋转后的坐标*/
    val _visualRotateBounds = RectF()

    //</editor-fold desc="属性">

    //<editor-fold desc="计算属性">

    val _tempMatrix = Matrix()
    val _rotateMatrix: Matrix = Matrix()

    //</editor-fold desc="计算属性">

    override fun getRotateBounds(): RectF = _rotateBounds

    override fun getRendererRotateBounds(): RectF = _rotateRenderBounds

    override fun getVisualRotateBounds(): RectF = _visualRotateBounds

    override fun changeBounds(block: RectF.() -> Unit) {
        getBounds().block()
        onItemBoundsChanged()
        //notify
        canvasViewBox.canvasView.dispatchItemBoundsChanged(this)
    }

    override fun updateRendererItem(item: T?, oldItem: T?) {
        super.updateRendererItem(item, oldItem)
        rendererItem = item
    }

    override fun updateLockScaleRatio(lock: Boolean) {
        isLockScaleRatio = lock
    }

    override fun onCanvasBoxMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        onItemBoundsChanged()
    }

    /**当渲染的bounds改变后, 需要主动触发此方法, 用来更新辅助bounds*/
    override fun onItemBoundsChanged() {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRendererBounds())
        canvasViewBox.calcItemVisualBounds(getRendererBounds(), getVisualBounds())

        mapRotateRect(getBounds(), getRotateBounds())
        mapRotateRect(getRendererBounds(), getRendererRotateBounds())
        mapRotateRect(getVisualBounds(), getVisualRotateBounds())
    }

    fun getRotateMatrix(rotateCenterX: Float, rotateCenterY: Float): Matrix {
        _rotateMatrix.reset()
        _rotateMatrix.postRotate(rotate, rotateCenterX, rotateCenterY)
        return _rotateMatrix
    }

    override fun mapRotatePoint(
        rotateCenterX: Float,
        rotateCenterY: Float,
        point: PointF,
        result: PointF
    ): PointF {
        return getRotateMatrix(rotateCenterX, rotateCenterY).mapPoint(point, result)
    }

    override fun mapRotateRect(
        rotateCenterX: Float,
        rotateCenterY: Float,
        rect: RectF,
        result: RectF
    ): RectF {
        return getRotateMatrix(rotateCenterX, rotateCenterY).mapRectF(rect, result)
    }

    override fun mapRotateRect(rect: RectF, result: RectF): RectF {
        val rendererBounds = getRendererBounds()
        return mapRotateRect(rendererBounds.centerX(), rendererBounds.centerY(), rect, result)
    }

    override fun mapRotatePoint(point: PointF, result: PointF): PointF {
        val rendererBounds = getRendererBounds()
        return mapRotatePoint(rendererBounds.centerX(), rendererBounds.centerY(), point, result)
    }

    val rotatePath: Path = Path()

    override fun containsPoint(point: PointF): Boolean {
        val rendererBounds = getRendererBounds()
        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        }
    }

    override fun containsRect(rect: RectF): Boolean {
        val rendererBounds = getRendererBounds()
        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(rect)
        }
    }

    //<editor-fold desc="控制方法">

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    override fun translateBy(distanceX: Float, distanceY: Float) {
        changeBounds {
            offset(distanceX, distanceY)
        }
    }

    /**缩放元素, 在元素左上角位置开始缩放
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离
     * [withCenter] 缩放缩放是否使用中点坐标, 默认是左上角
     * */
    override fun scaleBy(scaleX: Float, scaleY: Float, withCenter: Boolean) {
        _tempMatrix.reset()
        this.scaleX *= scaleX
        this.scaleY *= scaleY
        changeBounds {
            val x = if (withCenter) centerX() else left
            val y = if (withCenter) centerY() else top
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.postScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }
    }

    override fun scaleTo(scaleX: Float, scaleY: Float, withCenter: Boolean) {
        _tempMatrix.reset()
        //复原矩形
        val bounds = getBounds()
        val oldScaleX = this.scaleX
        val oldScaleY = this.scaleY
        bounds.apply {
            val x = if (withCenter) centerX() else left
            val y = if (withCenter) centerY() else top
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.postScale(1f / oldScaleX, 1f / oldScaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }

        this.scaleX = scaleX
        this.scaleY = scaleY
        changeBounds {
            val x = if (withCenter) centerX() else left
            val y = if (withCenter) centerY() else top
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.setScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }
    }

    /**旋转元素, 旋转操作不能用matrix, 不能将操作数据更新到bounds
     * [degrees] 旋转的角度*/
    override fun rotateBy(degrees: Float) {
        /*_tempMatrix.reset()
       getRendererBounds().apply {
           _tempMatrix.postRotate(degrees, centerX(), centerY())
           _tempMatrix.mapRect(this, this)
       }*/
        changeBounds {
            rotate += degrees
            rotate %= 360
        }
    }

    override fun updateBounds(width: Float, height: Float, withCenter: Boolean) {
        L.i("调整宽高->w:$width h:${height} $withCenter")
        changeBounds {
            //如果有旋转角度, 要确保旋转前后的锚点坐标一直, 体验才好
            //原始矩形旋转的中心点坐标
            val originCenterX = centerX()
            val originCenterY = centerY()
            //左上角固定, 调整矩形宽高
            adjustSizeWithLT(width, height)

            //按照原始的旋转中点坐标, 旋转调整后的矩形
            val rotateRect = _tempRectF
            val matrix = _tempMatrix
            _tempMatrix.reset()
            matrix.postRotate(rotate, originCenterX, originCenterY)
            matrix.mapRectF(this, rotateRect)

            //旋转后的矩形中点就是调整后的矩形需要偏移的x,y
            offset(rotateRect.centerX() - centerX(), rotateRect.centerY() - centerY())
        }
    }

    //</editor-fold desc="控制方法">

}
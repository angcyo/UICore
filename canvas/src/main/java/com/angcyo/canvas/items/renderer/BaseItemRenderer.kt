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
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF
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
            if (old != value && value != null) {
                onUpdateRendererItem(value)
            }
        }

    /**[_bounds]旋转后的坐标*/
    val _rotateBounds = RectF()

    /**[_visualBounds]旋转后的坐标*/
    val _visualRotateBounds = RectF()

    //</editor-fold desc="属性">

    //<editor-fold desc="计算属性">

    val _tempMatrix = Matrix()
    val _rotateMatrix: Matrix = Matrix()
    val rotateMatrix: Matrix
        get() {
            val rendererBounds = getRendererBounds()
            _rotateMatrix.reset()
            _rotateMatrix.postRotate(rotate, rendererBounds.centerX(), rendererBounds.centerY())
            return _rotateMatrix
        }

    //</editor-fold desc="计算属性">

    override fun getRendererRotateBounds(): RectF = _rotateBounds

    override fun getVisualRotateBounds(): RectF = _visualRotateBounds

    override fun onUpdateRendererItem(item: T) {
        super.onUpdateRendererItem(item)
    }

    override fun updateLockScaleRatio(lock: Boolean) {
        isLockScaleRatio = lock
    }

    override fun onCanvasMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
        //super.onCanvasMatrixUpdate(canvasView, matrix, oldValue)
        onRendererBoundsChanged()
    }

    /**当渲染的bounds改变后, 需要主动触发此方法, 用来更新辅助bounds*/
    override fun onRendererBoundsChanged() {
        canvasViewBox.calcItemVisibleBounds(this, _visualBounds)
        mapRotateRect(getRendererBounds(), _rotateBounds)
        mapRotateRect(getVisualBounds(), _visualRotateBounds)
    }

    override fun mapRotatePoint(point: PointF, result: PointF): PointF {
        return rotateMatrix.mapPoint(point, result)
    }

    override fun mapRotateRect(rect: RectF, result: RectF): RectF {
        return rotateMatrix.mapRectF(rect, result)
    }

    val rotatePath: Path = Path()

    override fun containsPoint(point: PointF): Boolean {
        val rendererBounds = getRendererBounds()
        return rotateMatrix.run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        }
    }

    override fun containsRect(rect: RectF): Boolean {
        val rendererBounds = getRendererBounds()
        return rotateMatrix.run {
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
        _tempMatrix.reset()
        _tempMatrix.postTranslate(distanceX, distanceY)
        getRendererBounds().apply {
            _tempMatrix.mapRect(this, this)
            onRendererBoundsChanged()
        }
    }

    /**缩放元素, 在元素左上角位置开始缩放
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离
     * [widthCenter] 缩放缩放是否使用中点坐标, 默认是左上角
     * */
    override fun scaleBy(scaleX: Float, scaleY: Float, widthCenter: Boolean) {
        _tempMatrix.reset()
        this.scaleX *= scaleX
        this.scaleY *= scaleY
        getRendererBounds().apply {
            val x = if (widthCenter) centerX() else left
            val y = if (widthCenter) centerY() else top
            _tempPoint.set(x, y)
            mapRotatePoint(_tempPoint, _tempPoint)
            _tempMatrix.postScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
            onRendererBoundsChanged()
        }
    }

    /**旋转元素, 旋转操作不能用matrix, 不能将操作数据更新到bounds
     * [degrees] 旋转的角度*/
    override fun rotateBy(degrees: Float) {
        rotate += degrees
        rotate %= 360
        /*_tempMatrix.reset()
       getRendererBounds().apply {
           _tempMatrix.postRotate(degrees, centerX(), centerY())
           _tempMatrix.mapRect(this, this)
       }*/
        onRendererBoundsChanged()
    }

    //</editor-fold desc="控制方法">

}
package com.angcyo.canvas.items.renderer

import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.ICanvasItem
import com.angcyo.canvas.utils._tempMatrix
import com.angcyo.canvas.utils._tempPoint

/**
 * 绘制在[CanvasView]上的具体项目
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IItemRenderer<T : ICanvasItem> : IRenderer {

    /**需要渲染的item*/
    var rendererItem: T?

    /**是否锁定了缩放比例*/
    var isLockScaleRatio: Boolean

    /**当[rendererItem]需要更新时触发, 用来更新渲染器*/
    fun onUpdateRendererItem(item: T) {
        //重新设置尺寸等信息
    }

    /**通过此方法更新[isLockScaleRatio]属性*/
    fun updateLockScaleRatio(lock: Boolean) {
        isLockScaleRatio = lock
    }

    /**当渲染的bounds改变后回调
     * [com.angcyo.canvas.core.IRenderer.getRendererBounds]*/
    fun onRendererBoundsChanged(afterBounds: RectF) {

    }

    /**控制点操作结束后回调*/
    fun onControlFinish(controlPoint: ControlPoint) {

    }

    //<editor-fold desc="控制方法">

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    fun translateBy(distanceX: Float, distanceY: Float) {
        _tempMatrix.reset()
        _tempMatrix.postTranslate(distanceX, distanceY)
        getRendererBounds().apply {
            _tempMatrix.mapRect(this, this)
            onRendererBoundsChanged(this)
        }
    }

    /**缩放元素, 在元素左上角位置开始缩放
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离
     * [centerX] 缩放的中点坐标, 默认是左上角
     * [centerY]
     * */
    fun scaleBy(scaleX: Float, scaleY: Float, widthCenter: Boolean = false) {
        _tempMatrix.reset()
        getRendererBounds().apply {
            val x = if (widthCenter) centerX() else left
            val y = if (widthCenter) centerY() else top
            _tempPoint.set(x, y)
            mapRotatePoint(_tempPoint, _tempPoint)
            _tempMatrix.postScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
            onRendererBoundsChanged(this)
        }
    }

    /**旋转元素, 旋转操作不能用matrix
     * [degrees] 旋转的角度*/
    fun rotateBy(degrees: Float) {
        rendererItem?.apply {
            rotate += degrees
            rotate %= 360
        }
        /*_tempMatrix.reset()
        getRendererBounds().apply {
            _tempMatrix.postRotate(degrees, centerX(), centerY())
            _tempMatrix.mapRect(this, this)
        }*/
    }

    //</editor-fold desc="控制方法">

    //<editor-fold desc="操作方法">

    /**当前的绘制item, 是否包含指定坐标
     * [point] 坐标系中的坐标, 非视图系的坐标*/
    fun containsPoint(point: PointF): Boolean

    /**当前的绘制item, 是否包含指定矩形坐标
     * [rect] 坐标系中的坐标, 非视图系的坐标*/
    fun containsRect(rect: RectF): Boolean

    /**根据[rotate]映射点*/
    fun mapRotatePoint(point: PointF, result: PointF): PointF

    /**根据[rotate]映射矩形*/
    fun mapRotateRect(rect: RectF, result: RectF): RectF

    //</editor-fold desc="操作方法">
}
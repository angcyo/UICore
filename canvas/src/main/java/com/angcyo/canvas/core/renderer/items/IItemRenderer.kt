package com.angcyo.canvas.core.renderer.items

import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.items.ICanvasItem
import com.angcyo.canvas.utils._tempMatrix

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

    /**当[rendererItem]需要更新时触发, 用来更新渲染器*/
    fun onUpdateRendererItem(item: T) {
        //重新设置尺寸等信息
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
        }
    }

    /**缩放元素
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离*/
    fun scaleBy(scaleX: Float, scaleY: Float) {
        _tempMatrix.reset()
        getRendererBounds().apply {
            _tempMatrix.postScale(scaleX, scaleY, centerX(), centerY())
            _tempMatrix.mapRect(this, this)
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

    /**根据[rotate]映射点*/
    fun mapRotatePoint(point: PointF, result: PointF): PointF

    /**根据[rotate]映射矩形*/
    fun mapRotateRect(rect: RectF, result: RectF): RectF

    //</editor-fold desc="操作方法">
}
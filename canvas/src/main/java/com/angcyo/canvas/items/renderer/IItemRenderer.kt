package com.angcyo.canvas.items.renderer

import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.ICanvasItem
import com.angcyo.library.ex.ADJUST_TYPE_LT

/**
 * 绘制在[CanvasView]上的具体项目
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IItemRenderer<T : ICanvasItem> : IRenderer {

    //<editor-fold desc="bounds">

    /**旋转后的坐标
     * [getBounds]*/
    fun getRotateBounds(): RectF

    /**旋转后的坐标
     * [getRenderBounds]*/
    fun getRenderRotateBounds(): RectF

    /**旋转后的坐标
     * [getVisualBounds]*/
    fun getVisualRotateBounds(): RectF

    /**当[rendererItem]需要更新时触发, 用来更新渲染器*/
    fun onUpdateRendererItem(item: T?, oldItem: T? = null) {
        //重新设置尺寸等信息
    }

    /**当渲染的bounds改变后回调. 进行了平移, 缩放, 旋转等操作后.
     * 需要主动触发此方法, 用来更新辅助bounds.
     *
     * 可以在此方法中限制bounds大小
     *
     * [com.angcyo.canvas.core.IRenderer.getBounds]*/
    fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {

    }

    //</editor-fold desc="bounds">

    //<editor-fold desc="控制点回调">

    /**控制点操作之前的回调*/
    fun onControlStart(controlPoint: ControlPoint) {

    }

    /**控制点操作结束后的回调*/
    fun onControlFinish(controlPoint: ControlPoint) {

    }

    /**当当前的渲染器被取消了选中状态时回调
     * [toSelectedItem] 被新选中的渲染器
     * [com.angcyo.canvas.CanvasDelegate.selectedItem]*/
    fun onCancelSelected(toSelectedItem: BaseItemRenderer<*>?) {

    }

    /**当渲染器被添加到画布时的回调*/
    fun onAddRenderer() {

    }

    /**当渲染器被移除画布时的回调*/
    fun onRemoveRenderer() {

    }

    //</editor-fold desc="控制点回调">

    //<editor-fold desc="控制方法">

    /**通过此方法更新[isLockScaleRatio]属性*/
    fun updateLockScaleRatio(lock: Boolean)

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    fun translateBy(distanceX: Float, distanceY: Float)

    /**缩放元素, 在元素左上角位置开始缩放
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离
     * [withCenter] 缩放缩放是否使用中点坐标, 默认是左上角
     * */
    fun scaleBy(scaleX: Float, scaleY: Float, adjustType: Int = ADJUST_TYPE_LT)

    /**缩放到指定比例*/
    fun scaleTo(scaleX: Float, scaleY: Float, adjustType: Int = ADJUST_TYPE_LT)

    /**更新bounds到指定的宽高*/
    fun updateBounds(width: Float, height: Float, adjustType: Int = ADJUST_TYPE_LT)

    /**旋转元素, 旋转操作不能用matrix
     * [degrees] 旋转的角度*/
    fun rotateBy(degrees: Float)

    //</editor-fold desc="控制方法">

    //<editor-fold desc="操作方法">

    /**当前的绘制item, 是否包含指定坐标
     * [point] 坐标系中的坐标, 非视图系的坐标
     * [containsRect]
     * [intersectRect]*/
    fun containsPoint(point: PointF): Boolean

    /**当前的绘制item, 是否包含指定矩形坐标
     * [rect] 坐标系中的坐标, 非视图系的坐标
     * [containsPoint]*/
    fun containsRect(rect: RectF): Boolean

    /**当前的绘制item, 是否和指定的矩形相交
     * [containsRect]
     * [containsPoint]*/
    fun intersectRect(rect: RectF): Boolean

    /**根据[rotate]映射点*/
    fun mapRotatePoint(
        rotateCenterX: Float,
        rotateCenterY: Float,
        point: PointF,
        result: PointF
    ): PointF

    /**根据[rotate]映射矩形*/
    fun mapRotateRect(rotateCenterX: Float, rotateCenterY: Float, rect: RectF, result: RectF): RectF

    fun mapRotateRect(rect: RectF, result: RectF): RectF

    fun mapRotatePoint(point: PointF, result: PointF): PointF

    //</editor-fold desc="操作方法">

    //<editor-fold desc="其他方法">

    /**是否支持指定的控制点
     * [com.angcyo.canvas.core.component.ControlPoint.POINT_TYPE_DELETE]
     * [com.angcyo.canvas.core.component.ControlPoint.POINT_TYPE_ROTATE]
     * [com.angcyo.canvas.core.component.ControlPoint.POINT_TYPE_SCALE]
     * [com.angcyo.canvas.core.component.ControlPoint.POINT_TYPE_LOCK]
     * */
    fun isSupportControlPoint(type: Int): Boolean {
        return true
    }

    //</editor-fold desc="其他方法">
}
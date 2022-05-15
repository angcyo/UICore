package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.Reason
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface ICanvasView : IComponent {

    //<editor-fold desc="dispatch">

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChangeBefore(matrix: Matrix, newValue: Matrix) {}

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix) {}

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.changeBounds]*/
    fun dispatchItemBoundsChanged(item: IRenderer, reason: Reason, oldBounds: RectF) {}

    /**当[CanvasViewBox]的坐标系原点改变时触发*/
    fun dispatchCoordinateSystemOriginChanged(point: PointF) {}

    /**当[CanvasViewBox]的坐标单位改变时触发*/
    fun dispatchCoordinateSystemUnitChanged(valueUnit: IValueUnit) {}

    /**分发回退/恢复栈发生改变
     * [CanvasUndoManager]*/
    fun dispatchCanvasUndoChanged() {}

    /**分发渲染器可见性改变*/
    fun dispatchItemVisibleChanged(item: IRenderer, visible: Boolean) {}

    /**[com.angcyo.canvas.core.component.control.LockControlPoint.onClickControlPoint]*/
    fun dispatchItemLockScaleRatioChanged(item: BaseItemRenderer<*>) {}

    //</editor-fold desc="dispatch">

    //<editor-fold desc="listener">

    /**[ICanvasListener]*/
    fun addCanvasListener(listener: ICanvasListener) {}

    /**[ICanvasListener]*/
    fun removeCanvasListener(listener: ICanvasListener) {}

    //</editor-fold desc="listener">

    //<editor-fold desc="operate">

    /**刷新*/
    fun refresh()

    /**获取视图盒子*/
    fun getCanvasViewBox(): CanvasViewBox

    /**通过手势坐标, 查找对应的[BaseItemRenderer]*/
    fun findItemRenderer(touchPoint: PointF): BaseItemRenderer<*>? = null

    /**回退管理*/
    fun getCanvasUndoManager(): CanvasUndoManager

    //</editor-fold desc="operate">

}
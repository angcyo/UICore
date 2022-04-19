package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface ICanvasView : IComponent {

    /**刷新*/
    fun refresh()

    //<editor-fold desc="dispatch">

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChangeBefore(matrix: Matrix, newValue: Matrix) {}

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix) {}

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.changeBounds]*/
    fun dispatchItemBoundsChanged(item: BaseItemRenderer<*>) {}

    /**当[CanvasViewBox]的坐标系原点改变时触发*/
    fun dispatchCoordinateSystemOriginChanged(point: PointF) {}

    //</editor-fold desc="dispatch">

    //<editor-fold desc="listener">

    /**[ICanvasListener]*/
    fun addCanvasListener(listener: ICanvasListener) {}

    /**[ICanvasListener]*/
    fun removeCanvasListener(listener: ICanvasListener) {}

    //</editor-fold desc="listener">

    //<editor-fold desc="operate">

    /**通过手势坐标, 查找对应的[BaseItemRenderer]*/
    fun findItemRenderer(touchPoint: PointF): BaseItemRenderer<*>? = null

    //</editor-fold desc="operate">

}
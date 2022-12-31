package com.angcyo.canvas.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.Reason
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.unit.IValueUnit

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
interface ICanvasView : IComponent {

    //<editor-fold desc="core">

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)

    fun onTouchEvent(event: MotionEvent): Boolean

    fun onDraw(canvas: Canvas)

    //</editor-fold desc="core">

    //<editor-fold desc="dispatch">

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChangeBefore(matrix: Matrix, newValue: Matrix) {}

    /**[com.angcyo.canvas.core.CanvasViewBox.refresh]*/
    fun dispatchCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix, isEnd: Boolean) {}

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.changeBoundsAction]*/
    fun dispatchItemBoundsChanged(itemRenderer: IRenderer, reason: Reason, oldBounds: RectF) {}

    /**当[CanvasViewBox]的坐标系原点改变时触发*/
    fun dispatchCoordinateSystemOriginChanged(point: PointF) {}

    /**当[CanvasViewBox]的坐标单位改变时触发*/
    fun dispatchCoordinateSystemUnitChanged(oldValueUnit: IValueUnit, valueUnit: IValueUnit) {}

    /**分发回退/恢复栈发生改变
     * [CanvasUndoManager]*/
    fun dispatchCanvasUndoChanged() {}

    /**分发渲染器可见性改变
     * [com.angcyo.canvas.core.ICanvasListener.onRenderItemVisibleChanged]*/
    fun dispatchItemVisibleChanged(item: IRenderer, visible: Boolean) {}

    /**分发渲染更新
     * [com.angcyo.canvas.core.ICanvasListener.onItemRenderUpdate]*/
    fun dispatchItemRenderUpdate(item: IRenderer) {}

    /**[com.angcyo.canvas.core.component.control.LockControlPoint.onClickControlPoint]*/
    fun dispatchItemLockScaleRatioChanged(item: BaseItemRenderer<*>) {}

    /**当图层的顺序发生了改变
     * [com.angcyo.canvas.CanvasDelegate.arrangeSort]*/
    fun dispatchItemSortChanged(itemList: List<BaseItemRenderer<*>>) {}

    /**当有item的数据发生改变后触发, 此时可以触发数据保存提示*/
    fun dispatchItemDataChanged(itemRenderer: IItemRenderer<*>, reason: Reason) {}

    /**分发当[itemRenderer]类型改变了, 比如从文本数据变成了图片数据*/
    fun dispatchItemTypeChanged(itemRenderer: IItemRenderer<*>) {}

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
    fun findItemRenderer(touchPoint: PointF): BaseItemRenderer<*>? =
        findItemRendererList(touchPoint).firstOrNull()

    /**[findItemRenderer] 获取一组*/
    fun findItemRendererList(touchPoint: PointF): List<BaseItemRenderer<*>> = emptyList()

    /**回退管理*/
    fun getCanvasUndoManager(): CanvasUndoManager

    //</editor-fold desc="operate">

}
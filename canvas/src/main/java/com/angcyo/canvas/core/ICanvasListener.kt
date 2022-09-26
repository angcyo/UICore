package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.Reason
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ICanvasListener : ICanvasTouch {

    //<editor-fold desc="绘制相关">

    /**[CanvasView]改变[Matrix]之前回调*/
    fun onCanvasBoxMatrixChangeBefore(matrix: Matrix, newValue: Matrix) {}

    /**[CanvasView]改变[Matrix]之后回调*/
    fun onCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix) {}

    //</editor-fold desc="绘制相关">

    //<editor-fold desc="Item相关">

    /**选中[IItemRenderer], 有可能[oldItemRenderer]会等于[itemRenderer]
     * [itemRenderer] 有可能为空, 为空表示没有选中
     * [com.angcyo.canvas.CanvasDelegate.selectedItem]
     * */
    fun onSelectedItem(itemRenderer: IItemRenderer<*>?, oldItemRenderer: IItemRenderer<*>?) {}

    /**清除选中[IItemRenderer]
     * [itemRenderer] 之前选中的渲染项
     * [com.angcyo.canvas.CanvasDelegate.selectedItem]*/
    fun onClearSelectItem(itemRenderer: IItemRenderer<*>) {}

    /**[IItemRenderer]改变[Bounds]之后的回调*/
    fun onItemBoundsChanged(itemRenderer: IRenderer, reason: Reason, oldBounds: RectF) {}

    /**当[item]渲染需要更新时, 触发.
     * 比如大小改变, 颜色改变, 样式改变等*/
    fun onItemRenderUpdate(itemRenderer: IRenderer) {}

    /**可见性改变回调*/
    fun onItemVisibleChanged(itemRenderer: IRenderer, visible: Boolean) {}

    /**双击[IItemRenderer]*/
    fun onDoubleTapItem(itemRenderer: IItemRenderer<*>) {}

    /**[com.angcyo.canvas.CanvasDelegate.addItemRenderer]*/
    fun onItemRendererAdd(itemRenderer: IItemRenderer<*>) {}

    /**[com.angcyo.canvas.CanvasDelegate.removeItemRenderer]*/
    fun onItemRendererRemove(itemRenderer: IItemRenderer<*>) {}

    /**[com.angcyo.canvas.core.component.control.LockControlPoint.onClickControlPoint]*/
    fun onItemLockScaleRatioChanged(item: BaseItemRenderer<*>) {}

    /**[com.angcyo.canvas.core.ICanvasView.dispatchItemSortChanged]*/
    fun onItemSortChanged(itemList: List<BaseItemRenderer<*>>) {}

    //</editor-fold desc="Item相关">

    //<editor-fold desc="其他">

    /**回退/恢复栈发生改变后的回调
     * [CanvasUndoManager]*/
    fun onCanvasUndoChanged(undoManager: CanvasUndoManager) {}

    //</editor-fold desc="其他">

}
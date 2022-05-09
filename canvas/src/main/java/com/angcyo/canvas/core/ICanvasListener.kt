package com.angcyo.canvas.core

import android.graphics.Matrix
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

    /**选中[IItemRenderer], 有可能[oldItemRenderer]会等于[itemRenderer]*/
    fun onSelectedItem(itemRenderer: IItemRenderer<*>, oldItemRenderer: IItemRenderer<*>?) {}

    /**清除选中[IItemRenderer]*/
    fun onClearSelectItem(itemRenderer: IItemRenderer<*>) {}

    /**[IItemRenderer]改变[Bounds]之后的回调*/
    fun onItemBoundsChanged(itemRenderer: IItemRenderer<*>) {}

    /**双击[IItemRenderer]*/
    fun onDoubleTapItem(itemRenderer: IItemRenderer<*>) {}

    /**[com.angcyo.canvas.CanvasDelegate.addItemRenderer]*/
    fun onItemRendererAdd(itemRenderer: IItemRenderer<*>) {}

    /**[com.angcyo.canvas.CanvasDelegate.removeItemRenderer]*/
    fun onItemRendererRemove(itemRenderer: IItemRenderer<*>) {}

    //</editor-fold desc="Item相关">

    //<editor-fold desc="其他">

    /**回退/恢复栈发生改变后的回调
     * [CanvasUndoManager]*/
    fun onCanvasUndoChanged(undoManager: CanvasUndoManager) {}

    //</editor-fold desc="其他">

}
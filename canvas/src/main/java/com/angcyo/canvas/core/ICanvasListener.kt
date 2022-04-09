package com.angcyo.canvas.core

import android.graphics.Matrix
import android.view.MotionEvent
import com.angcyo.canvas.core.renderer.items.IItemRenderer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ICanvasListener {

    //<editor-fold desc="绘制相关">

    /**[CanvasView]改变[Matrix]之前回调*/
    fun onCanvasMatrixChangeBefore(matrix: Matrix) {

    }

    /**[CanvasView]改变[Matrix]之后回调*/
    fun onCanvasMatrixChangeAfter(matrix: Matrix, oldValue: Matrix) {

    }

    /**[MotionEvent]事件回调*/
    fun onCanvasTouchEvent(event: MotionEvent) {

    }

    //</editor-fold desc="绘制相关">

    //<editor-fold desc="Item相关">

    /**选中[IItemRenderer]*/
    fun onSelectedItem(itemRenderer: IItemRenderer, oldItemRenderer: IItemRenderer?) {

    }

    /**清除选中[IItemRenderer]*/
    fun onClearSelectItem(itemRenderer: IItemRenderer) {

    }

    //</editor-fold desc="Item相关">

}
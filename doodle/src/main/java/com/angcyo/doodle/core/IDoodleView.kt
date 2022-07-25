package com.angcyo.doodle.core

import android.graphics.Canvas
import android.view.MotionEvent
import com.angcyo.doodle.layer.BaseLayer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IDoodleView {

    //<editor-fold desc="core">

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)

    fun onTouchEvent(event: MotionEvent): Boolean

    fun onDraw(canvas: Canvas)

    //</editor-fold desc="core">

    //<editor-fold desc="layer">

    /**派发操作的图层改变通知*/
    fun dispatchOperateLayerChanged(from: BaseLayer?, to: BaseLayer?)

    /**派发操作的笔刷改变通知*/
    fun dispatchTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?)

    /**分发回退/恢复栈发生改变
     * [DoodleUndoManager]*/
    fun dispatchDoodleUndoChanged()

    //</editor-fold desc="layer">

    //<editor-fold desc="operate">

    /**刷新界面*/
    fun refresh()

    //</editor-fold desc="operate">

}
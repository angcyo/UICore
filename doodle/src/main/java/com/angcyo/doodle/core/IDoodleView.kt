package com.angcyo.doodle.core

import android.graphics.Canvas
import android.view.MotionEvent
import com.angcyo.doodle.element.BaseElement
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

    //<editor-fold desc="layer/element">

    /**派发操作的图层改变通知*/
    fun dispatchOperateLayerChanged(from: BaseLayer?, to: BaseLayer?)

    /**派发操作的笔刷改变通知*/
    fun dispatchTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?)

    /**分发回退/恢复栈发生改变
     * [DoodleUndoManager]*/
    fun dispatchDoodleUndoChanged()

    /**分发创建[BaseElement]
     * [element] 创建的颜色
     * [brush] 手势识别器/画笔类型等*/
    fun dispatchCreateElement(element: BaseElement, brush: ITouchRecognize?)

    /**派发元素[elementList]追加到[layer]图层中*/
    fun dispatchElementAttach(elementList: List<BaseElement>, layer: BaseLayer)

    /**派发元素[elementList]从[layer]图层中移除*/
    fun dispatchElementDetach(elementList: List<BaseElement>, layer: BaseLayer)

    //</editor-fold desc="layer/element">

    //<editor-fold desc="operate">

    /**刷新界面*/
    fun refresh()

    //</editor-fold desc="operate">

}
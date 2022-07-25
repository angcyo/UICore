package com.angcyo.doodle

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.angcyo.doodle.core.*
import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BackgroundLayer
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.longFeedback

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleDelegate(val view: View) : IDoodleView {

    //region ---成员---

    /**事件回调*/
    val doodleListenerList = mutableSetOf<IDoodleListener>()

    //endregion ---成员---

    //region ---核心成员---

    /**视口*/
    var viewBox = DoodleViewBox(this)

    /**回退栈管理*/
    var undoManager = DoodleUndoManager(this)

    /**手势管理*/
    var doodleTouchManager = DoodleTouchManager(this)

    /**图层管理*/
    var doodleLayerManager = DoodleLayerManager(this)

    //endregion ---核心成员---

    //region ---成员变量---

    /**当前操作的图层*/
    var operateLayer: BaseLayer?
        get() = doodleLayerManager.operateLayer
        set(value) = doodleLayerManager.updateOperateLayer(value)

    //endregion ---成员变量---

    //region ---入口---

    @CallPoint
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewBox.onSizeChanged(w, h, oldw, oldh)
    }

    @CallPoint
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return doodleTouchManager.onTouchEvent(event)
    }

    @CallPoint
    override fun onDraw(canvas: Canvas) {
        doodleLayerManager.onDraw(canvas)
    }

    //endregion ---入口---

    //region ---Core---

    override fun dispatchOperateLayerChanged(from: BaseLayer?, to: BaseLayer?) {
        doodleListenerList.forEach {
            it.onOperateLayerChanged(from, to)
        }
    }

    override fun dispatchTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?) {
        doodleListenerList.forEach {
            it.onTouchRecognizeChanged(from, to)
        }
    }

    override fun dispatchDoodleUndoChanged() {
        doodleListenerList.forEach {
            it.onDoodleUndoChanged(undoManager)
        }
    }

    //endregion ---Core---

    //region ---操作方法---

    /**刷新界面*/
    override fun refresh() {
        view.postInvalidateOnAnimation()
    }

    //endregion ---操作方法---

    //region ---operate---

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    fun addLayer() {

    }

    /**添加一个元素*/
    fun addElement(element: BaseElement, strategy: Strategy = Strategy.Normal()) {
        if (doodleLayerManager.layerList.isEmpty()) {
            //添加一个默认的背景层
            doodleLayerManager.addLayer(BackgroundLayer(this), Strategy.Redo())
        }
        operateLayer?.addElement(element, strategy)
    }

    //endregion ---operate---
}
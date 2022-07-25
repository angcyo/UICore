package com.angcyo.doodle.layer

import android.graphics.Canvas
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.core.Strategy
import com.angcyo.doodle.element.BaseElement

/**
 * 基础层
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseLayer(val doodleDelegate: DoodleDelegate) : ILayer {

    /**所有待绘制的元素列表*/
    val elementList = mutableListOf<BaseElement>()

    override fun onDraw(canvas: Canvas) {
        for (element in elementList) {
            element.onDraw(this, canvas)
        }
    }

    //region ---元素操作---

    /**添加一个元素*/
    fun addElement(element: BaseElement, strategy: Strategy) {
        doodleDelegate.undoManager.addAndRedo(strategy, {
            removeElement(element, it)
        }) {
            if (!elementList.contains(element)) {
                elementList.add(element)
                doodleDelegate.refresh()
            }
        }
    }

    /**一个绘制的元素*/
    fun removeElement(element: BaseElement, strategy: Strategy) {
        val index = elementList.indexOf(element)
        doodleDelegate.undoManager.addAndRedo(strategy, {
            elementList.add(index, element)
            doodleDelegate.refresh()
        }) {
            elementList.remove(element)
            doodleDelegate.refresh()
        }
    }

    //endregion ---元素操作---

}
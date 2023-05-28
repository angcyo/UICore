package com.angcyo.doodle.layer

import android.graphics.Canvas
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.element.BaseElement
import com.angcyo.library.component.Strategy
import com.angcyo.library.ex.resetAll

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

    /**清除图层上的所有元素*/
    fun clearAllElement(strategy: Strategy = Strategy.normal) {
        val oldList = elementList.toList()
        doodleDelegate.undoManager.addAndRedo(strategy, {
            elementList.resetAll(oldList)
            doodleDelegate.refresh()
            //notify
            doodleDelegate.dispatchElementAttach(oldList, this)
        }) {
            elementList.clear()
            doodleDelegate.refresh()
            //notify
            doodleDelegate.dispatchElementDetach(oldList, this)
        }
    }

    /**重置图层上的所有元素*/
    fun resetAllElement(newList: List<BaseElement>, strategy: Strategy = Strategy.normal) {
        val oldList = elementList.toList()
        doodleDelegate.undoManager.addAndRedo(strategy, {
            elementList.resetAll(oldList)
            doodleDelegate.refresh()
        }) {
            elementList.resetAll(newList)
            doodleDelegate.refresh()
        }
    }

    /**添加一个元素*/
    fun addElement(element: BaseElement, strategy: Strategy) {
        doodleDelegate.undoManager.addAndRedo(strategy, {
            removeElement(element, it)
        }) {
            if (!elementList.contains(element)) {
                elementList.add(element)
                element.onAddToLayer(this)
                doodleDelegate.refresh()

                //notify
                doodleDelegate.dispatchElementAttach(listOf(element), this)
            }
        }
    }

    /**一个绘制的元素*/
    fun removeElement(element: BaseElement, strategy: Strategy) {
        val index = elementList.indexOf(element)
        doodleDelegate.undoManager.addAndRedo(strategy, {
            elementList.add(index, element)
            doodleDelegate.refresh()
            //notify
            doodleDelegate.dispatchElementAttach(listOf(element), this)
        }) {
            elementList.remove(element)
            element.onRemoveFromLayer(this)
            doodleDelegate.refresh()
            //notify
            doodleDelegate.dispatchElementDetach(listOf(element), this)
        }
    }

    //endregion ---元素操作---

}
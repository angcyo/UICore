package com.angcyo.doodle.core

import android.graphics.Canvas
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.layer.BackgroundLayer
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.Strategy
import com.angcyo.library.ex.saveLayerAlpha

/**
 *
 * 图层管理, 所有层在此管理, 层上还会有很多元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleLayerManager(val doodleDelegate: DoodleDelegate) {

    /**背景层*/
    var backgroundLayer: BaseLayer? = BackgroundLayer(doodleDelegate)

    /**当前选中需要操作的层*/
    var operateLayer: BaseLayer? = null

    /**层的列表*/
    val layerList = mutableListOf<BaseLayer>()

    @CallPoint
    fun onDraw(canvas: Canvas) {
        //背景层
        backgroundLayer?.onDraw(canvas)

        //其他层
        for (layer in layerList) {
            onDrawLayer(canvas, layer)
        }
    }

    /**绘制指定图层*/
    fun onDrawLayer(canvas: Canvas, layer: BaseLayer) {
        val saveCount = canvas.saveLayerAlpha(255)
        layer.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    /**所有图层中, 是否有元素*/
    fun haveElement(): Boolean {
        val count = layerList.sumOf { it.elementList.size }
        return count > 0
    }

    //region ---图层操作---

    /**更新当前操作的图层*/
    fun updateOperateLayer(layer: BaseLayer?) {
        val old = operateLayer
        operateLayer = layer
        if (old != operateLayer) {
            doodleDelegate.dispatchOperateLayerChanged(old, operateLayer)
        }
    }

    /**添加一个图层*/
    fun addLayer(layer: BaseLayer, strategy: Strategy) {
        val isEmpty = layerList.isEmpty()

        doodleDelegate.undoManager.addAndRedo(strategy, {
            removeLayer(layer, it)
        }) {
            layerList.add(layer)
            doodleDelegate.refresh()
            doodleDelegate.dispatchLayerAdd(layer)
        }

        if (isEmpty) {
            //默认的第一个是操作图层
            updateOperateLayer(layer)
        }
    }

    /**移除一个图层*/
    fun removeLayer(layer: BaseLayer, strategy: Strategy) {
        val index = layerList.indexOf(layer)
        doodleDelegate.undoManager.addAndRedo(strategy, {
            layerList.add(index, layer)
            doodleDelegate.refresh()
        }) {
            layerList.remove(layer)
            doodleDelegate.refresh()
            doodleDelegate.dispatchLayerRemove(layer)
        }

        if (operateLayer == layer) {
            //选择最后一个图层
            updateOperateLayer(layerList.lastOrNull())
        }
    }

    /**隐藏背景层*/
    fun hideBackgroundLayer(hide: Boolean = true, strategy: Strategy) {
        if (!hide) {
            if (backgroundLayer != null) {
                //已经有背景
                return
            }
        }

        val old = backgroundLayer
        doodleDelegate.undoManager.addAndRedo(strategy, {
            backgroundLayer = old
            doodleDelegate.refresh()
        }) {
            backgroundLayer = if (hide) {
                null
            } else {
                old ?: BackgroundLayer(doodleDelegate)
            }
            doodleDelegate.refresh()
        }
    }

    //endregion ---图层操作---

}
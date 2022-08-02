package com.angcyo.doodle.core

import android.graphics.Canvas
import androidx.core.graphics.withSave
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.layer.BackgroundLayer
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.annotation.CallPoint

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
            canvas.withSave {
                layer.onDraw(canvas)
            }
        }
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
        }

        if (operateLayer == layer) {
            //选择最后一个图层
            updateOperateLayer(layerList.lastOrNull())
        }
    }

    //endregion ---图层操作---

}
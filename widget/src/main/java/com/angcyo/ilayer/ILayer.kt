package com.angcyo.ilayer

import com.angcyo.ilayer.container.IContainer
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.widget.DslViewHolder

/**
 * 视图小部件, 类似于[IView], 比[IView]更轻量.
 * 同一个[ILayer]对象, 可以同时显示在多个[IContainer]中
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class ILayer {

    /**布局id, 这是必须的*/
    var iLayerLayoutId: Int = -1

    //<editor-fold desc="生命周期方法">

    /**生命周期1*/
    open fun onCreate(container: IContainer, viewHolder: DslViewHolder) {
        L.i("${this.simpleHash()} ${container.simpleHash()}")
    }

    /**生命周期2*/
    open fun onInitLayer(container: IContainer, viewHolder: DslViewHolder, params: LayerParams) {
        L.i("${this.simpleHash()} ${container.simpleHash()}")
    }

    /**生命周期3
     * [fromContainer] 从什么容器中移除
     * */
    open fun onDestroy(fromContainer: IContainer, viewHolder: DslViewHolder) {
        L.i("${this.simpleHash()} ${fromContainer.simpleHash()}")
    }

    //</editor-fold desc="生命周期方法">

    //<editor-fold desc="操作方法">

    /**在指定的[container]中显示[ILayer]*/
    fun show(container: IContainer) {
        container.add(this)
    }

    fun hide(container: IContainer) {
        container.remove(this)
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="内部方法">


    //</editor-fold desc="内部方法">
}
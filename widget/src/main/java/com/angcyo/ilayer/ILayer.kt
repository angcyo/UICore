package com.angcyo.ilayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angcyo.ilayer.container.BaseContainer
import com.angcyo.ilayer.container.IContainer
import com.angcyo.ilayer.container.IDragConstraint
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.widget.DslViewHolder

/**
 * 视图小部件, 类似于[IView], 比[IView]更轻量.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class ILayer {

    /**布局id, 这是必须的*/
    var iLayerLayoutId: Int = -1

    /**是否要激活拖拽*/
    var enableDrag = false

    /**是否长按才激活拖拽*/
    var enableLongPressDrag = false
        set(value) {
            field = value
            if (value) {
                enableDrag = true
            }
        }

    /**是否自动保存和恢复位置*/
    var autoRestorePosition: Boolean = true

    /**是否要在拖拽时, 显示销毁窗口
     * [WindowContainer] 支持*/
    var showCancelLayer: Boolean = false

    /**渲染界面*/
    var renderLayer: (DslViewHolder.() -> Unit)? = null

    //<editor-fold desc="生命周期方法">

    var _rootView: View? = null

    var _container: IContainer? = null

    /**创建根视图*/
    open fun onCreateView(context: Context, container: IContainer): View {
        val rootView = _rootView
        if (rootView != null && container == _container) {
            //在同一个容器中, 使用缓存view, 否则创建新的
            return rootView
        }

        _container = container

        val dragFrameLayout = DragFrameLayout(context)

        _rootView = if (enableDrag) {

            dragFrameLayout.layoutParams = ViewGroup.LayoutParams(-2, -2)
            dragFrameLayout.enableLongPressDrag = enableLongPressDrag

            dragFrameLayout.dragAction = { distanceX, distanceY, end ->
                onDragBy(distanceX, distanceY, end)
            }

            LayoutInflater.from(context)
                .inflate(iLayerLayoutId, dragFrameLayout, true)
        } else {
            LayoutInflater.from(context)
                .inflate(iLayerLayoutId, dragFrameLayout, false)
        }

        return _rootView!!
    }

    /**生命周期1*/
    open fun onCreate(viewHolder: DslViewHolder) {
        L.d(this.simpleHash())
    }

    /**生命周期2*/
    open fun onInitLayer(viewHolder: DslViewHolder, params: LayerParams) {
        L.d(this.simpleHash())
        renderLayer?.invoke(viewHolder)
    }

    /**生命周期3
     * [fromContainer] 从什么容器中移除
     * */
    open fun onDestroy(fromContainer: IContainer, viewHolder: DslViewHolder) {
        L.d("${this.simpleHash()}")
        _container = null
    }

    //</editor-fold desc="生命周期方法">

    //<editor-fold desc="操作方法">

    /**在指定的[container]中显示[ILayer]*/
    fun show(container: IContainer? = null) {
        if (_container != null && _container != container) {
            hide(_container!!)
        }
        container?.add(this)
    }

    /**从指定的[container]中移除[ILayer]*/
    fun hide(container: IContainer) {
        container.remove(this)
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="内部方法">

    /**拖拽约束*/
    var dragContainer: IDragConstraint? = null

    /**位置拖动*/
    open fun onDragBy(dx: Float, dy: Float, end: Boolean) {
        val container = _container
        if (container is BaseContainer) {
            container.onDragBy(this, dx, dy, end)
        }
    }

    //</editor-fold desc="内部方法">
}
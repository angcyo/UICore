package com.angcyo.ilayer.container

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angcyo.ilayer.DragFrameLayout
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.LayerParams
import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW
import com.angcyo.widget.base.setDslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseContainer(val context: Context) : IContainer {

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

    override fun add(layer: ILayer) {
        if (layer.iLayerLayoutId == -1) {
            L.e("请配置[iLayerLayoutId]")
        } else {
            var rootView: View? = getRootView(layer)
            if (rootView == null) {
                //不存在旧的, 重新创建

                //1:创建根视图
                val view = onCreateRootView(layer)

                //请勿覆盖tag
                view.tag = layer.iLayerLayoutId

                rootView = view

                val viewHolder = DslViewHolder(view)
                view.setDslViewHolder(viewHolder)

                layer.onCreate(this, viewHolder)

                //2:添加根视图
                onAddRootView(layer, view)
            } else {
                //已经存在, 重新初始化
            }
            layer.onInitLayer(this, rootView.dslViewHolder(), LayerParams())
        }
    }

    override fun remove(layer: ILayer) {
        val rootView: View? = getRootView(layer)
        if (rootView == null) {
            L.w("[layer]已不在已移除.")
        } else {
            val dslViewHolder = rootView.dslViewHolder()
            layer.onDestroy(this, dslViewHolder)
            dslViewHolder.clear()
            rootView.setDslViewHolder(null)
            onRemoveRootView(layer, rootView)
        }
    }

    /**创建根视图*/
    open fun onCreateRootView(layer: ILayer): View {
        val dragFrameLayout = DragFrameLayout(context)

        return if (enableDrag) {

            dragFrameLayout.layoutParams = ViewGroup.LayoutParams(-2, -2)
            dragFrameLayout.enableLongPressDrag = enableLongPressDrag

            dragFrameLayout.dragAction = { distanceX, distanceY, end ->
                onDragBy(layer, distanceX, distanceY, end)
            }

            LayoutInflater.from(context)
                .inflate(layer.iLayerLayoutId, dragFrameLayout, true)
        } else {
            LayoutInflater.from(context)
                .inflate(layer.iLayerLayoutId, dragFrameLayout, false)
        }
    }

    /**将[rootView]添加到任意地方*/
    abstract fun onAddRootView(layer: ILayer, rootView: View)

    /**移除[rootView]*/
    abstract fun onRemoveRootView(layer: ILayer, rootView: View)

    /**位置拖动*/
    open fun onDragBy(layer: ILayer, dx: Float, dy: Float, end: Boolean) {

    }

    /**更新布局位置, 使用视图左上角定位, 计算出对应的[gravity]和边界百分比*/
    fun parseLayerPosition(
        layer: ILayer,
        maxWidth: Int,
        maxHeight: Int,
        left: Float,
        top: Float
    ): OffsetPosition {
        val hGravity: Int
        val offsetX: Float

        if (left < maxWidth / 2) {
            hGravity = Gravity.LEFT
            offsetX = left * 1f / maxWidth
        } else {
            hGravity = Gravity.RIGHT
            offsetX = (maxWidth - left - getRootView(layer).mW()) * 1f / maxWidth
        }

        val vGravity: Int
        val offsetY: Float

        if (top < maxHeight / 2) {
            vGravity = Gravity.TOP
            offsetY = top * 1f / maxHeight
        } else {
            vGravity = Gravity.BOTTOM
            offsetY = (maxHeight - top - getRootView(layer).mH()) * 1f / maxHeight
        }

        val gravity = hGravity or vGravity
        return OffsetPosition(gravity, offsetX, offsetY)
    }
}
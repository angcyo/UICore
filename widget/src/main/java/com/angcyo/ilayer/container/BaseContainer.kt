package com.angcyo.ilayer.container

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.annotation.CallSuper
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.LayerParams
import com.angcyo.library.L
import com.angcyo.library.ex.className
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
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

    /**默认位置*/
    var defaultOffsetPosition: OffsetPosition = OffsetPosition()

    override fun add(layer: ILayer) {
        if (layer.iLayerLayoutId == -1) {
            L.e("请配置[iLayerLayoutId]")
        } else {
            val rootView: View = layer.onCreateView(context, this)
            val layerPosition: OffsetPosition?
            if (rootView.parent == null) {
                //不存在旧的, 重新创建

                //请勿覆盖tag
                rootView.tag = layer.iLayerLayoutId

                val viewHolder: DslViewHolder = rootView.dslViewHolder()
                layer.onCreate(viewHolder)

                //2:添加根视图
                onAddRootView(layer, rootView)

                var firstPosition: OffsetPosition = defaultOffsetPosition.copy()

                //自动恢复位置
                if (layer.autoRestorePosition) {
                    "${this.className()}_${layer.className()}_${layer.iLayerLayoutId}".hawkGet(null)
                        ?.let {
                            if (!it.isNullOrEmpty()) {
                                val split = it.split(":")
                                val position = OffsetPosition(
                                    split.getOrNull(0)?.toIntOrNull()
                                        ?: Gravity.TOP or Gravity.LEFT,
                                    split.getOrNull(1)?.toFloatOrNull() ?: 0f,
                                    split.getOrNull(2)?.toFloatOrNull() ?: 0.3f
                                )

                                firstPosition = position
                            }
                        }
                }
                layerPosition = firstPosition
            } else {
                //已经存在, 重新初始化
                layerPosition = null
            }
            layer.onInitLayer(rootView.dslViewHolder(), LayerParams())
            update(layer, layerPosition)
        }
    }

    override fun remove(layer: ILayer) {
        val rootView: View? = layer._rootView
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

    /**将[rootView]添加到任意地方*/
    abstract fun onAddRootView(layer: ILayer, rootView: View)

    /**移除[rootView]*/
    abstract fun onRemoveRootView(layer: ILayer, rootView: View)

    /**[ILayer]位置拖动处理*/
    open fun onDragBy(layer: ILayer, dx: Float, dy: Float, end: Boolean) {

    }

    @CallSuper
    override fun update(layer: ILayer, position: OffsetPosition?) {
        "${this.className()}_${layer.className()}_${layer.iLayerLayoutId}".hawkPut("${position?.gravity}:${position?.offsetX}:${position?.offsetY}")
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
            offsetX = (maxWidth - left - layer._rootView.mW()) * 1f / maxWidth
        }

        val vGravity: Int
        val offsetY: Float

        if (top < maxHeight / 2) {
            vGravity = Gravity.TOP
            offsetY = top * 1f / maxHeight
        } else {
            vGravity = Gravity.BOTTOM
            offsetY = (maxHeight - top - layer._rootView.mH()) * 1f / maxHeight
        }

        val gravity = hGravity or vGravity
        return OffsetPosition(gravity, offsetX, offsetY)
    }
}
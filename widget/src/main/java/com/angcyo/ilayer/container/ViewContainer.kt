package com.angcyo.ilayer.container

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.drawable.isLeft
import com.angcyo.drawable.isTop
import com.angcyo.ilayer.ILayer
import com.angcyo.library.L
import com.angcyo.library.ex.className
import com.angcyo.widget.base.frameParams
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW

/**
 * 在[ViewGroup]中显示[ILayer]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class ViewContainer(val parent: ViewGroup) : BaseContainer(parent.context) {

    val _containerRect: Rect = Rect()

    override fun onAddRootView(layer: ILayer, rootView: View) {
        parent.addView(rootView)
    }

    override fun onRemoveRootView(layer: ILayer, rootView: View) {
        parent.removeView(rootView)
    }

    override fun onDragBy(layer: ILayer, dx: Float, dy: Float, end: Boolean) {
        val rootView = layer._rootView ?: return

        if (end) {
            //保存位置
            val offsetPosition = parseLayerPosition(
                layer,
                parent.mW(),
                parent.mH(),
                rootView.left - dx,
                rootView.top - dy
            )
            layer.dragContainer?.onDragEnd(this, layer, offsetPosition) ?: update(
                layer,
                offsetPosition
            )
        } else {
            val left = (rootView.left - dx).toInt()
            val top = (rootView.top - dy).toInt()
            rootView.layout(left, top, left + rootView.mW(), top + rootView.mH())

            val gravity = if (parent is FrameLayout) {
                rootView.layoutParams.frameParams()!!.gravity
            } else {
                Gravity.LEFT or Gravity.TOP
            }

            layer.dragContainer?.onDragMoveTo(this, layer, gravity, left, top)
        }
    }

    override fun update(layer: ILayer, position: OffsetPosition?) {
        super.update(layer, position)

        val rootView = layer._rootView ?: return

        if (parent is FrameLayout) {
            position?.let {
                rootView.frameParams {
                    this.gravity = position.gravity
                    if (gravity.isLeft()) {
                        leftMargin = (position.offsetX * parent.mW()).toInt()
                        rootView.left = (position.offsetX * parent.mW()).toInt()
                        rightMargin = 0
                    } else {
                        rightMargin = (position.offsetX * parent.mW()).toInt()
                        leftMargin = 0
                    }

                    if (gravity.isTop()) {
                        topMargin = (position.offsetY * parent.mH()).toInt()
                        rootView.top = (position.offsetY * parent.mH()).toInt()
                        bottomMargin = 0
                    } else {
                        bottomMargin = (position.offsetY * parent.mH()).toInt()
                        topMargin = 0
                    }
                }
            }
        } else {
            L.w("不支持的容器[${parent.className()}]")
        }
    }

    override fun getContainerRect(): Rect {
        _containerRect.set(0, 0, parent.mW(), parent.mH())
        return _containerRect
    }
}
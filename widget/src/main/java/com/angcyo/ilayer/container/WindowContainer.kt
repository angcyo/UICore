package com.angcyo.ilayer.container

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.angcyo.drawable.isLeft
import com.angcyo.drawable.isTop
import com.angcyo.ilayer.CancelLayer
import com.angcyo.ilayer.ILayer
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW
import com.angcyo.widget.base.screenRect


/**
 * 悬浮窗容器, 可以显示在状态栏下面, 导航栏下面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class WindowContainer(context: Context) : BaseContainer(context) {

    val wm: WindowManager get() = context.getSystemService(WINDOW_SERVICE) as WindowManager

    //permission denied for window type 2001
    //type 至少从2002开始
    val wmLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT

        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        windowAnimations = 0

        gravity = Gravity.LEFT or Gravity.TOP
        x = 0
        y = _screenHeight / 3
        format = PixelFormat.RGBA_8888

        //大于8.0
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    var _cancelLayer: CancelLayer? = null

    override fun add(layer: ILayer) {
        if (layer.showCancelLayer) {
            if (_cancelLayer == null) {
                _cancelLayer = CancelLayer()
                _cancelLayer?.show()
            }
            _cancelLayer?.hide()
        }
        super.add(layer)
    }

    override fun onAddRootView(layer: ILayer, rootView: View) {
        try {
            wm.addView(rootView, wmLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRemoveRootView(layer: ILayer, rootView: View) {
        try {
            if (rootView.parent != null) {
                wm.removeView(rootView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLayout(layer: ILayer) {
        try {
            val rootView = layer._rootView ?: return
            if (rootView.parent != null) {
                wm.updateViewLayout(rootView, wmLayoutParams)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDragBy(layer: ILayer, dx: Float, dy: Float, end: Boolean) {
        val rootView = layer._rootView ?: return

        val gravity = wmLayoutParams.gravity

        if (gravity.isLeft()) {
            wmLayoutParams.x = (wmLayoutParams.x - dx).toInt()
        } else {
            wmLayoutParams.x = (wmLayoutParams.x + dx).toInt()
        }

        if (gravity.isTop()) {
            wmLayoutParams.y = (wmLayoutParams.y - dy).toInt()
        } else {
            wmLayoutParams.y = (wmLayoutParams.y + dy).toInt()
        }

        if (end) {
            //保存位置
            val left = if (gravity.isLeft()) {
                wmLayoutParams.x
            } else {
                _screenWidth - wmLayoutParams.x - rootView.mW()
            }

            val top = if (gravity.isTop()) {
                wmLayoutParams.y
            } else {
                _screenHeight - wmLayoutParams.y - rootView.mH()
            }

            val offsetPosition = parseLayerPosition(
                layer,
                _screenWidth,
                _screenHeight,
                left.toFloat(),
                top.toFloat()
            )

            layer.dragContainer?.onDragEnd(this, layer, offsetPosition) ?: update(
                layer,
                offsetPosition
            )

            //隐藏销毁提示layer
            if (layer.showCancelLayer && _cancelLayer?.cancelFlag == true) {
                layer.hide(this)
                _cancelLayer?.hide(true)
            } else {
                _cancelLayer?.hide(false)
            }

        } else {
            updateLayout(layer)
            layer.dragContainer?.onDragMoveTo(
                this,
                layer,
                wmLayoutParams.gravity,
                wmLayoutParams.x,
                wmLayoutParams.y
            )

            //显示销毁提示layer
            if (layer.showCancelLayer) {
                _cancelLayer?.targetMoveTo(
                    rootView.screenRect().centerX(),
                    rootView.screenRect().centerY()
                )
            }
        }
    }

    override fun update(layer: ILayer, position: OffsetPosition) {
        super.update(layer, position)
        layer._rootView?.let {
            wmLayoutParams.gravity = position.gravity
            wmLayoutParams.x = (_screenWidth * position.offsetX).toInt()
            wmLayoutParams.y = (_screenHeight * position.offsetY).toInt()

            updateLayout(layer)
        }
    }
}
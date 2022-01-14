package com.angcyo.ilayer.container

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.angcyo.drawable.isLeft
import com.angcyo.drawable.isTop
import com.angcyo.ilayer.CancelLayer
import com.angcyo.ilayer.ILayer
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.remove
import com.angcyo.tablayout.clamp
import com.angcyo.widget.base.*


/**
 * 悬浮窗容器, 可以显示在状态栏下面, 导航栏下面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class WindowContainer(context: Context) : BaseContainer(context) {

    val _containerRect: Rect = Rect()

    val wm: WindowManager get() = context.getSystemService(WINDOW_SERVICE) as WindowManager

    //permission denied for window type 2001
    //type 至少从2002开始
    val wmLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT

        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or    //范围外的触摸事件发送给后面的窗口处理
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or       //不获取焦点
            /*WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or  //布局在装饰条之外*/
            /*WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or    //不接受触摸屏事件*/
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or    //占满整个屏幕
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS       //允许到屏幕之外

        windowAnimations = 0

        gravity = defaultOffsetPosition.gravity
        x = (_screenWidth * defaultOffsetPosition.offsetX).toInt()
        y = (_screenHeight * defaultOffsetPosition.offsetY).toInt()
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

    val _rootRect = Rect()
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

        L.d("wm x:${wmLayoutParams.x} y:${wmLayoutParams.y} ${rootView.screenRect()}")

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
            val offsetPosition = if (dragWithGravity) {
                parseLayerPosition(
                    layer,
                    _screenWidth,
                    _screenHeight,
                    left.toFloat(),
                    top.toFloat()
                )
            } else {
                val l: Int
                val t: Int
                if (defaultOffsetPosition.reCenter) {
                    l = left + layer._rootView.mW() / 2
                    t = top + layer._rootView.mH() / 2
                } else {
                    l = left
                    t = top
                }

                OffsetPosition(
                    gravity,
                    clamp(l.toFloat() / _screenWidth, 0f, 1f),
                    clamp(t.toFloat() / _screenHeight, 0f, 1f)
                )
            }
            offsetPosition.reCenter = defaultOffsetPosition.reCenter

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

            onDragAction?.invoke(layer, offsetPosition, true)
        } else {
            updateLayout(layer)

            var left = wmLayoutParams.x
            var top = wmLayoutParams.y

            layer.dragContainer?.onDragMoveTo(
                this,
                layer,
                wmLayoutParams.gravity,
                left,
                top
            )

            //显示销毁提示layer
            if (layer.showCancelLayer) {
                _cancelLayer?.targetMoveTo(rootView.screenRect(_rootRect))
            }

            if (defaultOffsetPosition.reCenter) {
                left += layer._rootView.mW() / 2
                top += layer._rootView.mH() / 2
            }

            onDragAction?.invoke(
                layer,
                OffsetPosition(
                    gravity,
                    left * 1f / getContainerRect().width(),
                    top * 1f / getContainerRect().height(),
                    defaultOffsetPosition.reCenter
                ),
                false
            )
        }
    }

    override fun update(layer: ILayer, position: OffsetPosition?) {
        super.update(layer, position)
        layer._rootView?.let { view ->
            position?.let {
                wmLayoutParams.gravity = position.gravity
                val offsetX = (_screenWidth * position.offsetX).toInt()
                val offsetY = (_screenHeight * position.offsetY).toInt()
                wmLayoutParams.x =
                    if (position.reCenter) offsetX - view.mWOrMeasure() / 2 else offsetX
                wmLayoutParams.y =
                    if (position.reCenter) offsetY - view.mHOrMeasure() / 2 else offsetY
            }

            updateLayout(layer)
        }
    }

    override fun getContainerRect(): Rect {
        _containerRect.set(0, 0, _screenWidth, _screenHeight)
        return _containerRect
    }

    /**修改[WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE]
     * 使其无法接收touch事件*/
    fun notTouchable(value: Boolean, layer: ILayer) {
        val wmLayoutParams = wmLayoutParams
        if (value) {
            wmLayoutParams.flags = wmLayoutParams.flags or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        } else {
            wmLayoutParams.flags =
                wmLayoutParams.flags.remove(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        updateLayout(layer)
    }
}

/**[android.view.WindowManager.LayoutParams.flags]*/
fun Int.flagLayoutFullScreen() = this or
    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
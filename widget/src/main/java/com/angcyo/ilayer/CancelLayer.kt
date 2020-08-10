package com.angcyo.ilayer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.view.Gravity
import com.angcyo.ilayer.container.IContainer
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library.app
import com.angcyo.library.ex._color
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.*

/**
 * 用于提示滑动至此, 删除浮窗
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class CancelLayer : ILayer() {

    /**是否移动到了销毁区域*/
    var cancelFlag = false

    init {
        iLayerLayoutId = R.layout.layout_cancel_layer
    }

    val _windowContainer = WindowContainer(app()).apply {
        wmLayoutParams.width = -1
        wmLayoutParams.height = -2

        defaultOffsetPosition.gravity = Gravity.BOTTOM
        defaultOffsetPosition.offsetX = 0f
        defaultOffsetPosition.offsetY = 0f
    }

    fun show() {
        show(_windowContainer)
        _rootView?.visible()
    }

    fun hide(remove: Boolean = false) {
        if (remove) {
            hide(_windowContainer)
        } else {
            _rootView?.gone()
        }
    }

    override fun onDestroy(fromContainer: IContainer, viewHolder: DslViewHolder) {
        super.onDestroy(fromContainer, viewHolder)
        cancelFlag = false
    }

    /**调用此方法, 刷新界面*/
    fun targetMoveTo(targetRect: Rect) {
        show()
        _rootView?.tagDslViewHolder()?.img(R.id.lib_image_view)?.apply {
            val screenRect = screenRect()
            if (targetRect.intersect(screenRect)) {
                //相交
                val old = cancelFlag
                cancelFlag = true
                imageTintList = ColorStateList.valueOf(_color(R.color.error))
                if (!old) {
                    longFeedback()
                }
            } else {
                cancelFlag = false
                imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }
}
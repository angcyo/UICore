package com.angcyo.dialog.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.angcyo.dialog.PopupConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.dpi
import com.angcyo.widget.DslViewHolder

/**
 * 无焦点的[PopupWindow], 多用于提示信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class PopupTipConfig : PopupConfig() {

    /**将[PopupWindow]的x中心与这个坐标对齐*/
    var touchX: Float = 0f

    /**限制[PopupWindow]的x中心在锚点矩形内*/
    var limitTouchInAnchor: Boolean = true

    init {
        animationStyle = R.style.LibActionPopupAnimation
        focusable = false
        touchable = false
        outsideTouchable = false
        showWithActivity = false

        autoOffsetCenterInScreen = false
        autoOffsetCenterInAnchor = false

        //边距
        minHorizontalOffset = 0

        autoOffset = true
        offsetY = 2 * dpi
        gravity = Gravity.LEFT or Gravity.TOP

        //必须指定
        layoutId
    }

    override fun showWithPopupWindow(context: Context): PopupWindow {
        return super.showWithPopupWindow(context)
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initLayout(window, viewHolder)
        xoff = (touchX - rootViewRect.width() / 2).toInt()
        checkLimit()
    }

    override fun updatePopup(updateLayout: Boolean) {
        xoff = (touchX - rootViewRect.width() / 2).toInt()
        checkLimit()
        super.updatePopup(updateLayout)
    }

    /**检查边界限制*/
    fun checkLimit() {
        if (limitTouchInAnchor) {
            val minOffset = -rootViewRect.width() / 2
            val maxOffset = anchorViewRect.width() - rootViewRect.width() / 2
            xoff = clamp(xoff, minOffset, maxOffset)
        }
        //L.i(xoff)
    }
}

/**Dsl*/
@DSL
fun Context.popupTipWindow(
    anchor: View?,
    layoutId: Int,
    config: PopupTipConfig.() -> Unit
): TargetWindow {
    val popupConfig = PopupTipConfig()
    popupConfig.layoutId = layoutId
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}
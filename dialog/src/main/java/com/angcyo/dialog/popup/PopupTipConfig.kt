package com.angcyo.dialog.popup

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.angcyo.dialog.PopupConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.dismissWindow
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.isVisible
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

    /**需要限制[touchX]再次横向范围内, 默认是[anchorViewRect]*/
    var limitTouchRect: Rect? = null

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
        autoAdjustGravity = false
        offsetY = 2 * dpi
        gravity = Gravity.LEFT or Gravity.TOP

        //必须指定
        popupLayoutId
    }

    override fun showWithPopupWindow(context: Context): PopupWindow {
        return super.showWithPopupWindow(context)
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        super.initLayout(window, viewHolder)
        touchOffset()
    }

    override fun updatePopup(updateLayout: Boolean) {
        touchOffset()
        super.updatePopup(updateLayout)
    }

    /**自动偏移到手势的位置[touchX]*/
    fun touchOffset() {
        if (autoOffsetCenterInScreen || autoOffsetCenterInAnchor) {
            //no op
        } else {
            offsetX = (touchX - rootViewRect.width() / 2).toInt()
            checkLimit()
        }
    }

    /**检查边界限制*/
    fun checkLimit() {
        if (limitTouchInAnchor) {
            val rect = limitTouchRect ?: anchorViewRect
            val rootViewWidth = rootViewRect.width() //Popup内容view的宽度
            val minOffset = rect.left - rootViewWidth / 2
            val maxOffset = rect.right - rootViewWidth / 2
            offsetX = clamp(offsetX, minOffset, maxOffset)
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
    popupConfig.popupLayoutId = layoutId
    popupConfig.anchor = anchor
    popupConfig.config()
    return popupConfig.show(this)
}

/**Dsl
 * [delayDismiss] 多少毫秒后, 自动销毁*/
@DSL
fun View.popupTipWindow(
    text: CharSequence? = null,
    delayDismiss: Long = 3000,
    layoutId: Int = R.layout.lib_popup_tip_layout,
    gravity: Int? = null,
    config: PopupTipConfig.() -> Unit = {},
    initLayout: (window: TargetWindow, viewHolder: DslViewHolder) -> Unit = { _, _ -> }
): TargetWindow {
    if (!isVisible() || parent == null) {
        return "View not visible or parent is null."
    }
    val popupConfig = PopupTipConfig()
    popupConfig.autoOffsetCenterInAnchor = true //锚点控件居中显示
    popupConfig.offsetY = -36 * dpi //偏移阴影的位置
    popupConfig.anchor = this
    if (gravity != null) {
        popupConfig.autoAdjustGravity = false
        popupConfig.gravity = gravity
    }
    popupConfig.popupLayoutId = layoutId
    popupConfig.onInitLayout = { window, viewHolder ->
        viewHolder.tv(R.id.lib_text_view)?.text = text
        initLayout(window, viewHolder)
    }
    popupConfig.config()
    postDelayed({
        popupConfig._container?.dismissWindow()
    }, delayDismiss)
    return popupConfig.show(context)
}
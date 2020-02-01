package com.angcyo.dialog

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class PopupConfig {
    /**
     * 标准需要显示的属性
     * */
    var anchor: View? = null

    /**
     * 使用此属性, 将会使用 showAtLocation(View parent, int gravity, int x, int y) 显示window
     *
     * 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
     * */
    var parent: View? = null

    var xoff: Int = 0
    var yoff: Int = 0

    //此属性 似乎只在 showAtLocation 有效, 在showAsDropDown中, anchor完全在屏幕底部, 系统会控制在TOP显示, 手动控制无效
    var gravity = Gravity.NO_GRAVITY//Gravity.TOP or Gravity.START or Gravity.LEFT

    /**
     * 标准属性
     * */
    var contentView: View? = null
    var height = WindowManager.LayoutParams.WRAP_CONTENT
    var width = WindowManager.LayoutParams.WRAP_CONTENT
    var focusable = true
    var touchable = true
    var outsideTouchable = true
    var background: Drawable? = null

    /**将[height]设置为, 锚点距离屏幕*/
    var exactlyHeight = false

    /**
     * 动画样式, 0 表示没有动画, -1 表示 默认动画.
     * */
    var animationStyle = R.style.LibPopupAnimation

    /**
     * 指定布局id
     * */
    var layoutId: Int = -1

    //自动会赋值
    var popupViewHolder: DslViewHolder? = null

    /**
     * 回调
     * */
    var onDismiss: (popupWindow: PopupWindow) -> Unit = {}

    var popupInit: (popupWindow: PopupWindow, popupViewHolder: DslViewHolder) -> Unit = { _, _ -> }

    open fun onPopupInit(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {

    }
}
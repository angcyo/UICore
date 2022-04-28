package com.angcyo.dialog.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.angcyo.dialog.PopupConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.WindowClickAction
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.find
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW
import com.angcyo.widget.DslViewHolder
import com.angcyo.library.ex.adjustOrder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.resetChild
import kotlin.math.max

/**
 * 类似qq聊天中, 弹出的Popup菜单
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ActionPopupConfig : PopupConfig() {

    /**需要填充的Action布局*/
    var actionItemLayout: Int = R.layout.lib_popup_action_item

    /**动作*/
    val actionList = mutableListOf<PopupAction>()

    init {
        layoutId = R.layout.lib_popup_action_layout

        animationStyle = R.style.LibActionPopupAnimation
        autoOffset = true
        autoOffsetCenterInScreen = false
        background = ColorDrawable(Color.TRANSPARENT)
        yoff = 4 * dpi
    }

    override fun initLayout(window: Any, viewHolder: DslViewHolder) {

        viewHolder.group(R.id.lib_flow_layout)
            ?.resetChild(actionList, actionItemLayout) { itemView, item, itemIndex ->
                itemView.find<TextView>(R.id.lib_text_view)?.text = item.text
                itemView.clickIt {
                    if (item.autoDismiss && window is PopupWindow) {
                        window.dismiss()
                    }
                    item.action?.invoke(window, it)
                }
            }

        super.initLayout(window, viewHolder)

        val view = anchor
        if (view != null) {
            val rect = view.getViewRect()

            if (isAnchorInTopArea(view)) {
                //目标在屏幕的上半区
                viewHolder.group(R.id.lib_wrap_layout)
                    ?.adjustOrder(R.id.lib_triangle_view, R.id.lib_flow_layout)
                viewHolder.view(R.id.lib_triangle_view)?.rotation = 180f
            } else {
                //目标在屏幕的下半区

                //调整三角形的顺序和旋转角度
                viewHolder.group(R.id.lib_wrap_layout)
                    ?.adjustOrder(R.id.lib_flow_layout, R.id.lib_triangle_view)
                viewHolder.view(R.id.lib_triangle_view)?.rotation = 0f
            }

            //设置三角形的位置
            viewHolder.view(R.id.lib_triangle_view)?.apply {
                val w = mW()
                val h = mH()
                val lp = layoutParams
                if (lp is LinearLayout.LayoutParams) {
                    if (isAnchorInLeftArea(view)) {
                        lp.gravity = Gravity.LEFT
                        lp.leftMargin = max(rect.centerX() - rootViewRect.left - w / 2, 0)
                    } else {
                        lp.gravity = Gravity.RIGHT
                        lp.rightMargin = max(rootViewRect.right - rect.centerX() - w / 2, 0)
                    }
                }
                layoutParams = lp
            }
        }
    }

    /**添加[PopupAction]*/
    fun addAction(text: CharSequence?, autoDismiss: Boolean = true, action: WindowClickAction?) {
        actionList.add(PopupAction(text, autoDismiss, action))
    }
}

/** 展示一个 弹窗菜单 popup window */
fun Context.actionPopupWindow(anchor: View?, config: ActionPopupConfig.() -> Unit): Any {
    val popupConfig = ActionPopupConfig()
    popupConfig.anchor = anchor

    //popupConfig.addAction()

    popupConfig.config()
    return popupConfig.show(this)
}
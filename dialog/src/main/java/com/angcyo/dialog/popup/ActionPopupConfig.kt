package com.angcyo.dialog.popup

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.WindowClickAction
import com.angcyo.library.ex.find
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.resetChild

/**
 * 类似qq聊天中, 弹出的Popup菜单
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ActionPopupConfig : AnchorPopupConfig() {

    /**需要填充的Action布局*/
    var actionItemLayout: Int = R.layout.lib_popup_action_item

    /**动作*/
    val actionList = mutableListOf<PopupAction>()

    init {
        layoutId = R.layout.lib_popup_action_layout
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        viewHolder.group(R.id.lib_triangle_content_layout)
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
    }

    /**添加[PopupAction]
     * [autoDismiss] 点击后, 是否自动销毁[TargetWindow]*/
    fun addAction(text: CharSequence?, autoDismiss: Boolean = true, action: WindowClickAction?) {
        actionList.add(PopupAction(text, autoDismiss, action))
    }
}

/** 展示一个 弹窗菜单 popup window */
fun Context.actionPopupWindow(anchor: View?, config: ActionPopupConfig.() -> Unit): Any {
    val popupConfig = ActionPopupConfig()
    popupConfig.anchor = anchor

    //popupConfig.addAction() //添加一个操作项

    popupConfig.config()
    return popupConfig.show(this)
}
package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import com.angcyo.widget.DslViewHolder

/**
 * 常见的消息弹窗
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/02/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class MessageDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**消息大图显示*/
    var dialogMessageLargeDrawable: Drawable? = null

    init {
        dialogLayoutId = R.layout.lib_dialog_message_layout
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)
        dialogWidth = -1
        dialogMessageGravity = Gravity.CENTER
        //消息弹窗, 默认只显示[positiveButtonText]按钮
        negativeButtonText = null //cancel
        neutralButtonText = null
    }

    override fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initControlLayout(dialog, dialogViewHolder)

        dialogViewHolder.visible(R.id.dialog_large_image_view, dialogMessageLargeDrawable != null)
        dialogViewHolder.img(R.id.dialog_large_image_view)
            ?.setImageDrawable(dialogMessageLargeDrawable)

        //3个按钮都没有文本, 隐藏底部控制栏
        if (positiveButtonText == null &&
            negativeButtonText == null &&
            neutralButtonText == null
        ) {
            dialogViewHolder.view(R.id.control_line_view)?.visibility = View.GONE
        }
    }

}
package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import com.angcyo.library.ex.hawkPut
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

    /**如果设置了此属性, 则会显示不提示*/
    var dialogNotPromptKey: String? = null

    /**是否不再提示*/
    var _dialogIsNotPrompt: Boolean = false

    init {
        dialogLayoutId = R.layout.lib_dialog_message_layout
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)
        dialogWidth = -1
        dialogMessageGravity = Gravity.CENTER
        //消息弹窗, 默认只显示[positiveButtonText]按钮
        negativeButtonText = null //cancel
        neutralButtonText = null
    }

    override fun onDialogDestroy(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.onDialogDestroy(dialog, dialogViewHolder)
        dialogNotPromptKey?.hawkPut(_dialogIsNotPrompt)
    }

    override fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initControlLayout(dialog, dialogViewHolder)

        dialogViewHolder.visible(R.id.dialog_large_image_view, dialogMessageLargeDrawable != null)
        dialogViewHolder.img(R.id.dialog_large_image_view)
            ?.setImageDrawable(dialogMessageLargeDrawable)

        //不再提示
        dialogViewHolder.visible(R.id.lib_not_prompt_box, !dialogNotPromptKey.isNullOrBlank())
        dialogViewHolder.check(R.id.lib_not_prompt_box, false) { checkView, isChecked ->
            _dialogIsNotPrompt = isChecked
        }

        //3个按钮都没有文本, 隐藏底部控制栏
        if (positiveButtonText == null &&
            negativeButtonText == null &&
            neutralButtonText == null
        ) {
            dialogViewHolder.view(R.id.control_line_view)?.visibility = View.GONE
        }
    }

}
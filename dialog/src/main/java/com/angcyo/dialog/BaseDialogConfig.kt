package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import com.angcyo.library.L
import com.angcyo.library.ex._string
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.setLeftIco

/**
 * 对话框基础控制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    /**隐藏标题栏下面的线*/
    var hideDialogTitleLine: Boolean = false

    /**消息体 左边的图标*/
    var dialogMessageLeftIco: Drawable? = null

    /**消息体的 文本的对齐方式, 默认使用xml中的定义*/
    var dialogMessageGravity: Int? = null

    init {
        positiveButtonText = _string(R.string.dialog_positive)
        negativeButtonText = _string(R.string.dialog_negative)

        onCancelListener = {
            L.i("$it is Cancel!")
        }

        onDismissListener = {
            L.i("$it is Dismiss!")
        }
    }

    /**
     * 对话框初始化方法
     * [initControlLayout]
     * */
    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //标题
        dialogViewHolder.tv(R.id.dialog_title_view)?.apply {
            visibility = if (dialogTitle == null) View.GONE else View.VISIBLE
            text = dialogTitle
        }

        //消息体
        dialogViewHolder.tv(R.id.dialog_message_view)?.apply {
            visibility = if (dialogMessage == null) View.GONE else View.VISIBLE
            text = dialogMessage

            dialogMessageGravity?.let {
                gravity = it
            }

            //ico
            dialogMessageLeftIco?.let { setLeftIco(it) }
        }

        //标题栏控制
        dialogViewHolder.visible(R.id.dialog_title_layout, dialogTitle != null)
        dialogViewHolder.gone(
            R.id.dialog_title_line_view,
            dialogTitle == null || hideDialogTitleLine
        )

        initControlLayout(dialog, dialogViewHolder)
    }

    /**in [initDialogView]*/
    open fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        //确定按钮
        val positiveButton = dialogViewHolder.view(R.id.dialog_positive_button)
        positiveButton?.apply {
            visibility = if (isGonePositiveButton) View.GONE else View.VISIBLE

            if (positiveButton is TextView) {
                positiveButton.text = positiveButtonText
            }

            clickIt {
                positiveButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //取消按钮
        val negativeButton = dialogViewHolder.view(R.id.dialog_negative_button)
        negativeButton?.apply {
            visibility = if (isGoneNegativeButton) View.GONE else View.VISIBLE
            if (negativeButton is TextView) {
                negativeButton.text = negativeButtonText
            }

            clickIt {
                negativeButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //中立按钮
        val neutralButton = dialogViewHolder.view(R.id.dialog_neutral_button)

        neutralButton?.apply {
            visibility = if (isGoneNeutralButton) View.GONE else View.VISIBLE
            if (neutralButton is TextView) {
                neutralButton.text = neutralButtonText
            }

            clickIt {
                neutralButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //3个按钮都没有文本, 隐藏底部控制栏
        if (isGoneControlButton) {
            dialogViewHolder.view(R.id.dialog_control_layout)?.visibility = View.GONE
        }
    }
}
package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import com.angcyo.library.L
import com.angcyo.library.ex._string
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt

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
     * */
    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //标题
        dialogViewHolder.tv(R.id.title_view)?.apply {
            visibility = if (dialogTitle == null) View.GONE else View.VISIBLE
            text = dialogTitle
        }

        //消息体
        dialogViewHolder.tv(R.id.message_view)?.apply {
            visibility = if (dialogMessage == null) View.GONE else View.VISIBLE
            text = dialogMessage
        }

        //标题栏控制
        dialogViewHolder.visible(R.id.title_layout, dialogTitle != null)
        dialogViewHolder.visible(R.id.title_line_view, dialogTitle != null && !hideDialogTitleLine)

        initControlLayout(dialog, dialogViewHolder)
    }

    open fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        //确定按钮
        dialogViewHolder.tv(R.id.positive_button)?.apply {
            visibility = if (positiveButtonText == null) View.GONE else View.VISIBLE
            text = positiveButtonText

            clickIt {
                positiveButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //取消按钮
        dialogViewHolder.tv(R.id.negative_button)?.apply {
            visibility = if (negativeButtonText == null) View.GONE else View.VISIBLE
            text = negativeButtonText

            clickIt {
                negativeButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //中立按钮
        dialogViewHolder.tv(R.id.dialog_neutral_button)?.apply {
            visibility = if (neutralButtonText == null) View.GONE else View.VISIBLE
            text = neutralButtonText

            clickIt {
                neutralButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }

        //3个按钮都没有文本, 隐藏底部控制栏
        if (positiveButtonText == null &&
            negativeButtonText == null &&
            neutralButtonText == null
        ) {
            dialogViewHolder.view(R.id.dialog_control_layout)?.visibility = View.GONE
        }
    }
}
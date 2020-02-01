package com.angcyo.dialog

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import com.angcyo.library.L
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseDialogConfig {

    companion object {
        const val DIALOG_TYPE_APPCOMPAT = 1
        const val DIALOG_TYPE_ALERT_DIALOG = 2
        /**需要[material]库支持*/
        const val DIALOG_TYPE_BOTTOM_SHEET_DIALOG = 3
    }

    var dialogLayoutId = R.layout.lib_dialog_normal_layout

    var dialogCancel = true
        set(value) {
            field = value
            if (!value) {
                dialogCanceledOnTouchOutside = false
            }
        }

    var dialogCanceledOnTouchOutside = true

    /**
     * 对话框的标题, 为null时, 标题栏会被 GONE
     * */
    var dialogTitle: CharSequence? = null

    /**
     * 对话框的消息内容, 为null时, 会被 GONE
     * */
    var dialogMessage: CharSequence? = null

    /**
     * 中立按钮文本, 为null时, 会被 GONE
     * */
    var neutralButtonText: CharSequence? = null
    var neutralButtonListener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    open fun neutralButton(
        text: CharSequence? = neutralButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        neutralButtonText = text
        neutralButtonListener = listener
    }

    /**
     * 取消按钮文本, 为null时, 会被 GONE
     * */
    var negativeButtonText: CharSequence? = "取消"
    var negativeButtonListener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit =
        { dialog, _ -> dialog.cancel() }

    open fun negativeButton(
        text: CharSequence? = negativeButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        negativeButtonText = text
        negativeButtonListener = listener
    }

    /**
     * 确定按钮文本, 为null时, 会被 GONE
     * */
    var positiveButtonText: CharSequence? = "确定"
    var positiveButtonListener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit =
        { dialog, _ -> dialog.dismiss() }

    open fun positiveButton(
        text: CharSequence? = positiveButtonText,
        listener: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit
    ) {
        positiveButtonText = text
        positiveButtonListener = listener
    }

    /**
     * 初始化回调方法
     * */
    var dialogInit: (dialog: Dialog, dialogViewHolder: DslViewHolder) -> Unit = { _, _ -> }

    /**
     * 对话框初始化方法
     * */
    open fun onDialogInit(dialog: Dialog, dialogViewHolder: DslViewHolder) {

        //标题
        dialogViewHolder.tv(R.id.dialog_title_view)?.apply {
            visibility = if (dialogTitle == null) View.GONE else View.VISIBLE
            text = dialogTitle
        }

        //消息体
        dialogViewHolder.tv(R.id.dialog_message_view)?.apply {
            visibility = if (dialogMessage == null) View.GONE else View.VISIBLE
            text = dialogMessage
        }

        initControlLayout(dialog, dialogViewHolder)
    }

    open fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        //确定按钮
        dialogViewHolder.tv(R.id.dialog_positive_button)?.apply {
            visibility = if (positiveButtonText == null) View.GONE else View.VISIBLE
            text = positiveButtonText

            clickIt {
                positiveButtonListener.invoke(dialog, dialogViewHolder)
            }
        }

        //取消按钮
        dialogViewHolder.tv(R.id.dialog_negative_button)?.apply {
            visibility = if (negativeButtonText == null) View.GONE else View.VISIBLE
            text = negativeButtonText

            clickIt {
                negativeButtonListener.invoke(dialog, dialogViewHolder)
            }
        }

        //中立按钮
        dialogViewHolder.tv(R.id.dialog_neutral_button)?.apply {
            visibility = if (neutralButtonText == null) View.GONE else View.VISIBLE
            text = neutralButtonText

            clickIt {
                neutralButtonListener.invoke(dialog, dialogViewHolder)
            }
        }

        //3个按钮都没有文本, 隐藏底部控制栏
        if (positiveButtonText == null &&
            negativeButtonText == null &&
            neutralButtonText == null
        ) {
            dialogViewHolder.tv(R.id.dialog_control_layout)?.visibility = View.GONE
        }
    }


    /**
     * 可以设置的监听回调
     * */
    var onDialogCancel: (dialog: Dialog) -> Unit = {}
    var onDialogDismiss: (dialog: Dialog) -> Unit = {}

    /**
     * 当调用dialog.cancel时, 此方法会回调, 并且 onDialogDismiss 也会回调
     * */
    open fun onDialogCancel(dialog: Dialog) {
        L.d("onDialogCancel")
    }

    /**
     * 当调用dialog.dismiss时, 此方法会回调, 并且 onDialogCancel 不会回调
     * */
    open fun onDialogDismiss(dialog: Dialog) {
        L.d("onDialogDismiss")
    }

    /**
     * 显示dialog的类型
     * [AppCompatDialog] [AlertDialog] [BottomSheetDialog]
     * */
    var dialogType = DIALOG_TYPE_APPCOMPAT

    var dialogWidth = undefined_res
    var dialogHeight = undefined_res
    var dialogGravity = undefined_res
    var dialogBgDrawable: Drawable? = null

    var windowFeature = Window.FEATURE_NO_TITLE
    /**正数表示addFlags, 负数表示clearFlags*/
    var windowFlags = intArrayOf()
}
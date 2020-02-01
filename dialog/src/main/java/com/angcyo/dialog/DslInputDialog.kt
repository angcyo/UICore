package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*
import com.angcyo.widget.edit.CharLengthFilter
import com.angcyo.widget.pager.TextIndicator

/**
 * 文本输入对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

open class DslInputDialog(context: Context) : DslDialog(context) {

    init {
        dialogLayoutId = R.layout.lib_dialog_input_layout
        canceledOnTouchOutside = false
        dialogWidth = 1
        dialogHeight = -2
        setDialogBgColor(Color.TRANSPARENT)
        dialogGravity = Gravity.BOTTOM
    }

    var maxInputLength = 0
    var inputViewHeight = -1
    /**
     * 文本框hint文本
     */
    var hintInputString = "请输入..."
    /**
     * 左上角标题提示
     */
    var tipInputString = ""
    /**
     * 缺省的文本框内容
     */
    var defaultInputString = ""
    var saveButtonText: CharSequence? = null
    var showSoftInput = false
    /**
     * 是否允许输入为空
     */
    var canInputEmpty = true
    var useCharLengthFilter = false

    var onInputListener: OnInputListener? = null

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        val editView = dialogViewHolder.ev(R.id.dialog_edit_text_view)
        val indicatorView: TextIndicator? =
            dialogViewHolder.v(R.id.dialog_single_text_indicator_view)
        //输入框标题栏提示文本
        val tipView: TextView? = dialogViewHolder.v(R.id.dialog_input_tip_view)
        if (!TextUtils.isEmpty(tipInputString)) {
            tipView?.visibility = View.VISIBLE
            tipView?.text = tipInputString
        }
        //空输入
        if (!canInputEmpty) {
            editView?.onTextChange {
                dialogViewHolder.enable(R.id.dialog_save_button, it.isNotBlank())
            }
            dialogViewHolder.enable(R.id.dialog_save_button, !defaultInputString.isNotBlank())
        }

        //输入限制
        if (maxInputLength >= 0) {
            if (useCharLengthFilter) {
                editView?.addFilter(CharLengthFilter(maxInputLength))
            } else {
                editView?.addFilter(InputFilter.LengthFilter(maxInputLength))
            }

            indicatorView?.visibility = View.VISIBLE
            indicatorView?.setupEditText(editView, maxInputLength)
        }

        //单行or多行
        if (inputViewHeight > 0) {
            editView?.setWidthHeight(height = inputViewHeight)
            editView?.gravity = Gravity.TOP
            editView?.setSingleLineMode(false)
        } else {
            editView?.gravity = Gravity.CENTER_VERTICAL
            editView?.setSingleLineMode(true)
        }

        editView?.hint = hintInputString
        editView?.setInputText(defaultInputString)
        if (saveButtonText != null) {
            dialogViewHolder.tv(R.id.dialog_save_button)?.text = saveButtonText
        }

        dialogViewHolder.click(R.id.dialog_save_button) {
            var canCancel = true
            if (onInputListener != null && editView != null) {
                canCancel =
                    !onInputListener!!.onSaveClick(dialogViewHolder, editView, editView.string())
            }
            if (canCancel) {
                if (onInputListener != null) {
                    onInputListener!!.onInputString(editView.string())
                }
                dialog.cancel()
            }
        }
        if (showSoftInput) {
            dialogViewHolder.post { editView?.showSoftInput() }
        }
        super.initDialogView(dialog, dialogViewHolder)
    }

    interface OnInputListener {

        /**是否要拦截输入框的保存操作*/
        fun onSaveClick(
            dialogViewHolder: DslViewHolder,
            editView: EditText,
            input: String
        ): Boolean {
            return false
        }

        /**输入返回*/
        fun onInputString(input: String)
    }
}

/**快速显示带有输入框的[AppCompatDialog]*/
fun dslInputDialog(context: Context, action: DslDialog.() -> Unit) {
    val dslDialog = DslInputDialog(context)
    dslDialog.action()
    dslDialog.showCompatDialog()
}
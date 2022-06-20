package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.text.method.DigitsKeyListener
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.showSoftInput
import com.angcyo.widget.edit.PasswordInputEditText

/**
 * 验证码/密码输入对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/19
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class VerifyCodeInputDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**
     * 默认是否显示键盘
     * */
    var showSoftInput = true

    /**延迟多久才显示软键盘*/
    var showSoftInputDelay = InputDialogConfig.DEFAULT_SOFT_INPUT_DELAY

    /**输入限制, 此属性和[inputType]互斥
     * [R.string.lib_number_digits]
     * [R.string.lib_password_digits]
     * [R.string.lib_en_digits]*/
    var digits: String? = null

    /**
     * 输入框内容回调
     * 返回true: 拦截默认操作
     * 返回false: 执行默认操作dismiss
     * */
    var onVerifyCodeResult: (dialog: Dialog, verifyCode: String) -> Boolean = { _, _ ->
        false
    }

    init {
        dialogLayoutId = R.layout.lib_dialog_verify_code_input_layout

        positiveButtonText = null
        //negativeButtonText = _string(R.string.dialog_negative)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val editView: PasswordInputEditText? = dialogViewHolder.v(R.id.password_input_edit)

        editView?.onPasswordInputListener = object : PasswordInputEditText.OnPasswordInputListener {
            override fun onPassword(password: String) {
                if (onVerifyCodeResult(dialog, password)) {
                    //被拦截
                } else {
                    dialog.hideSoftInput()
                    dialog.dismiss()
                }
            }
        }

        //digits 放在[inputType]后面
        digits?.let {
            editView?.keyListener = DigitsKeyListener.getInstance(it)
        }

        if (showSoftInput) {
            dialogViewHolder.postDelay(showSoftInputDelay) { editView?.showSoftInput() }
        }
    }
}
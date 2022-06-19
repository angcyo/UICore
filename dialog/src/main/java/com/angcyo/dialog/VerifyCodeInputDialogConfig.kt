package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.widget.DslViewHolder

/**
 * 验证码/密码输入对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/19
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class VerifyCodeInputDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    init {
        dialogLayoutId = R.layout.lib_dialog_verify_code_input_layout

        positiveButtonText = null
        //negativeButtonText = _string(R.string.dialog_negative)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

    }
}
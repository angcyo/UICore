package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class TextDialogConfig(context: Context? = null) : BaseDialogConfig(context),
    ITouchBackDialogConfig {
    init {
        dialogLayoutId = R.layout.lib_dialog_text_layout

        dialogTitle
        dialogMessage
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        initTouchBackLayout(dialog, dialogViewHolder)
    }
}
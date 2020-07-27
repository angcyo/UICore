package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class IosDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    init {
        dialogLayoutId = R.layout.lib_dialog_normal_ios_layout
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)

        animStyleResId = R.style.LibIosDialogAnimation
    }

    override fun initControlLayout(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initControlLayout(dialog, dialogViewHolder)
        //取消按钮
        dialogViewHolder.gone(R.id.line_view_v, negativeButtonText == null)
    }
}
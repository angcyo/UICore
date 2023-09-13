package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTouchBackDialogConfig(context: Context? = null) : DslDialogConfig(context),
    ITouchBackDialogConfig {

    init {
        cancelable = true
        canceledOnTouchOutside = true

        dialogWidth = -1
        dialogHeight = -2
        dialogGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        animStyleResId = R.style.LibDialogBottomTranslateAnimation
        setDialogBgColor(Color.TRANSPARENT)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //核心初始化
        initTouchBackLayout(dialog, dialogViewHolder)
    }
}
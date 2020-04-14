package com.angcyo.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

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
}
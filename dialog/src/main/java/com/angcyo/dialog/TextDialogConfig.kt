package com.angcyo.dialog

import android.content.Context

/**
 * 底部文本提示对话框, 支持touch back
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class TextDialogConfig(context: Context? = null) : BaseTouchBackDialogConfig(context) {
    init {
        dialogLayoutId = R.layout.lib_dialog_text_layout

        dialogTitle
        dialogMessage
    }
}
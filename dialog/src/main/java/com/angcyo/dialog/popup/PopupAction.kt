package com.angcyo.dialog.popup

import com.angcyo.dialog.WindowClickAction

/**
 * [ActionPopupConfig]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class PopupAction(
    /**显示的文本*/
    val text: CharSequence?,
    /**是否自动关闭弹窗*/
    val autoDismiss: Boolean = true,
    /**点击动作*/
    val action: WindowClickAction?,
)
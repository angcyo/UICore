package com.angcyo.dialog.popup

import com.angcyo.library.ex.ClickAction

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
    /**点击动作*/
    val action: ClickAction?
)
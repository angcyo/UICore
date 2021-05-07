package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.copy
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.syncBack
import com.angcyo.library.isMain

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class CopyAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_COPY)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val text =
            control.accSchedule.accParse.textParse.parse(action.subEnd(Action.ARG_SPLIT))
                .firstOrNull()
        if (!text.isNullOrEmpty()) {
            if (isMain()) {
                success = text.copy() == true
            } else {
                syncBack {
                    success = text.copy() == true
                }
            }
        }
        control.log("复制文本[$text]:$success")
    }
}
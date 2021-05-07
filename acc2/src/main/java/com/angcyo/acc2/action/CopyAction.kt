package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.copy
import com.angcyo.library.ex.subEnd
import com.angcyo.library.ex.syncToMain
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

        val textParse = control.accSchedule.accParse.textParse
        val text = textParse.parse(action.subEnd(Action.ARG_SPLIT)).firstOrNull()

        var throwable: Throwable? = null

        if (!text.isNullOrEmpty()) {
            if (isMain()) {
                try {
                    success = text.copy(throwable = true) == true
                } catch (e: Exception) {
                    throwable = e
                }
            } else {
                syncToMain {
                    try {
                        success = text.copy(throwable = true) == true
                    } catch (e: Exception) {
                        throwable = e
                    }
                }
            }
        }

        if (throwable == null) {
            control.log("复制文本[$text]:$success")
        } else {
            control.log("复制文本异常[$text]")
            throw throwable!!
        }
    }
}